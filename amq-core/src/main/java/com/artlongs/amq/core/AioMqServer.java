package com.artlongs.amq.core;

import com.artlongs.amq.core.aio.AioServer;
import com.artlongs.amq.http.AioHttpServer;
import com.artlongs.amq.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Func :MQ 服务端
 *
 * @author: leeton on 2019/2/22.
 */
public class AioMqServer {
    private static Logger logger = LoggerFactory.getLogger(AioMqServer.class);

    private AioMqServer() {
    }

    public static final AioMqServer instance = new AioMqServer();
    private AioServer aioServer = null;

    private HttpServer httpServer = null;

    private ExecutorService pool = Executors.newFixedThreadPool(MqConfig.inst.server_connect_thread_pool_size);

    public void start() {
        try {
            AioServer<ByteBuffer> aioServer = new AioServer(MqConfig.inst.host, MqConfig.inst.port, new MqProtocol(), new MqServerProcessor());
            aioServer.startCheckAlive(MqConfig.inst.start_check_client_alive);
            aioServer.startMonitorPlugin(MqConfig.inst.start_flow_monitor);
            aioServer.setResumeSubcribe(true);
            //
            pool.submit(aioServer);
            aioServer.start();
            this.aioServer = aioServer;
            //
            ProcessorImpl.INST.addMonitor(aioServer.getMonitor());
            //
            startAdmin();
            //
            scheduler();
            //
            startCommond();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void scheduler() {
        MqScheduler.inst.start();
    }

    public void startAdmin(){
        if (MqConfig.inst.start_mq_admin) {
            httpServer = AioHttpServer.instance;
//            HttpProcessor processor = httpServer.getHttpProcessor();
//            processor.addController(new QueryController().getControllers());
            httpServer.start();

        }
    }

    public void shutdown() {
        //关闭后台管理
        this.httpServer.shutdown();
        //关闭 MQ 服务
        ProcessorImpl.INST.shutdown();
        //关闭 AIO 服务器
        this.aioServer.shutdown();
        shutdownMe();
        shutdownOfWait();
        System.out.println("================= AMQ EXIT =================");
        System.out.println("AMQ 已安全退出.");
    }

    private void shutdownMe(){
        if(!pool.isTerminated()){
            pool.shutdownNow();
        }
        try {
            pool.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            pool.shutdownNow();
        }
    }

    private void shutdownOfWait(){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startCommond(){
        Scanner sc = new Scanner(System.in);
        while (true){
            sc.useDelimiter("/n");
            System.out.println();
            System.out.println("=======================================");
            System.out.println("AMQ已启动,(消息端口:"+MqConfig.inst.port+"),(管理端口:"+MqConfig.inst.admin_http_port+")");
            System.out.println("如果想安全退出,请输入命令: quit");
            System.out.println("=======================================");
            System.out.println();
            String quit = sc.nextLine();
            if(quit.equalsIgnoreCase("quit")){
                shutdown();
                sc.close();
                break;
            }

        }
    }



    public static void main(String[] args) throws IOException {
        AioMqServer.instance.start();
    }

}
