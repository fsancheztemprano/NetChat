package chat.core;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import tools.log.Flogger;

class ClientSocketManager extends AbstractSocketManager {

    private String hostname;
    private int port;

    private ClientCommandProcessor clientCommandProcessor;

    public ClientSocketManager() {

        hostname = Globals.DEFAULT_SERVER_HOSTNAME;
        port     = Globals.DEFAULT_SERVER_PORT;
    }

    public ClientSocketManager(String hostname, int port) {
        this.hostname = hostname;
        this.port     = port;
    }

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
            Flogger.atInfo().withCause(ce).log("ER-CSM-0002");
            stopSocketManager();
        } catch (IOException ioe) {
            Flogger.atInfo().withCause(ioe).log("ER-CSM-0001");
        } catch (Exception e) {
            Flogger.atInfo().withCause(e).log("ER-CSM-0000");
        }

    }

    @Override
    public synchronized void stopSocketManager() {
        if (isActive() || isSocketOpen()) {
            try {
                deactivateChildProcesses();
                closePool();
                closeSocket();
            } catch (Exception e) {
                Flogger.atInfo().withCause(e).log("ER-CSM-0003");
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
}