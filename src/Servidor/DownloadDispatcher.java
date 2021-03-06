package Servidor;

import java.util.*;

public class DownloadDispatcher implements Runnable{

    private List<Pedido> pedidos;
    private PedidosDownload pedidosAAtender;
    private int sameUserDownloads;
    private Musicas musicas;

    public DownloadDispatcher(PedidosDownload pedidos, Musicas musicas){
        this.pedidos = new ArrayList<>();
        this.pedidosAAtender = pedidos;
        this.sameUserDownloads = 0;
        this.musicas = musicas;
    }

    private void ronda() throws InterruptedException {
        //  username  #pedidosAtendidos
        Map<String, Integer> atendidos = new HashMap<>();

        ListIterator<Pedido> iter = this.pedidos.listIterator();
        while(iter.hasNext()){
            Pedido h = iter.next();

            String u = h.getUsername();
            // ver se o user ja foi atendido
            if (atendidos.containsKey(u)){
                int n = atendidos.get(u).intValue();
                if (n == this.sameUserDownloads) continue;
                else atendidos.replace(u, n++);
            }
            else atendidos.put(u, 1);
            // ver se o numero de downloads ja foi excedido

            this.pedidosAAtender.incDownload();

            // iniciar o download
            Thread downld = new Thread(new DownloadMaker(this.musicas, this.pedidosAAtender, h.getCs(),  h.getMusicId()));
            downld.start();

            // remover pedido da lista
            iter.remove();
        }

        System.out.println("Acabei uma ronda");

    }

    private int countUsers(){
        Map<String, String> users = new HashMap<>();

        for (Pedido p : this.pedidos){
            String u = p.getUsername();
            if (!users.containsKey(u));
                users.put(u, null);
        }

        return users.size();
    }

    private void setmaxusercount(int count){

        if (count >= 50 && count < 100) this.sameUserDownloads = 3;
        else if (count >= 100 && count < 200) this.sameUserDownloads = 4;
        else if (count >= 200 && count < 300) this.sameUserDownloads = 5;
        else if (count >= 300) this.sameUserDownloads = 6;
    }

    public void run() {
        try {
            while(true){
                // fazer get dos pedidos por atender
                // adicionar ao array atual
                this.pedidos.addAll(this.pedidosAAtender.getPedidos(this.pedidos.size()));
                // medir a quantidade de utilizadores na proxima ronda
                int users = countUsers();
                // calcular quantos downloads na mesma ronda que se pode fazer para o mesmo user
                setmaxusercount(users);
                // iniciar a ronda
                ronda();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
