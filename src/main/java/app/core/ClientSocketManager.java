package app.core;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import tools.log.Flogger;

public class ClientSocketManager extends AbstractSocketManager {

    private String hostname = Globals.DEFAULT_SERVER_HOSTNAME;
    private int port = Globals.DEFAULT_SERVER_PORT;

    private ClientCommandProcessor clientCommandProcessor;


    @Override
    public synchronized void startSocketManager() {
        managedSocket = new Socket();
        InetSocketAddress addr = new InetSocketAddress(hostname, port);
        log("Conectando: " + addr.getAddress());
        try {
            managedSocket.connect(addr, Globals.CLIENT_CONNECT_TIMEOUT);
            setStreams();
            setActive(true);
            log("Conectado: " + addr.getAddress());

            inboundCommandQueue  = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
            outboundCommandQueue = new ArrayBlockingQueue<>(Byte.MAX_VALUE);
            managerPool          = Executors.newFixedThreadPool(4);

            initializeChildProcesses();
            poolUpChildProcesses();
        } catch (ConnectException ce) {
            Flogger.atWarning().withCause(ce).log("ER-CSM-0002");
            stopSocketManager();
        } catch (IOException ioe) {
            Flogger.atWarning().withCause(ioe).log("ER-CSM-0001");
        } catch (Exception e) {
            Flogger.atWarning().withCause(e).log("ER-CSM-0000");
        }

    }

    @Override
    public void stopSocketManager() {
        if (isActive() || isSocketOpen()) {
            try {
                deactivateChildProcesses();
                closeSocket();
                closePool();

                managedSocket        = null;
                managerPool          = null;
                heartbeatPacket      = null;
                heartbeatDaemon      = null;
                commandReceiver      = null;
                commandTransmitter   = null;
                inboundCommandQueue  = null;
                outboundCommandQueue = null;
                inputStream          = null;
                outputStream         = null;
            } catch (Exception e) {
                Flogger.atWarning().withCause(e).log("ER-CSM-0003");
            } finally {
                log("Conexion Finalizada");
                setActive(false);
                listener = null;
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    protected void deactivateChildProcesses() {
        super.deactivateChildProcesses();
        if (clientCommandProcessor != null)
            clientCommandProcessor.setActive(false);
    }


    @Override
    protected void initializeChildProcesses() {
        clientCommandProcessor = new ClientCommandProcessor(this);
        super.initializeChildProcesses();
    }

    @Override
    protected void poolUpChildProcesses() {
        managerPool.submit(clientCommandProcessor);
        super.poolUpChildProcesses();
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    void notifyChatMessageReceived(String username, String message) {
        if (listener != null && listener instanceof IClientStatusListener) {
            ((IClientStatusListener) listener).onChatMessageReceived(username, message);
        }
    }
}
