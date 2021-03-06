package Servidor;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Musicas {
    private Map<Long, Musica> musicas;
    private Lock lock;

    private long musicId;

    private int MAXSIZE = 1500;

    private int numDownloads;

    private String pastaBD = "C:\\Users\\luisb\\Documents\\GitHub\\SD\\musics\\";

    public Musicas(){
        this.musicas = new HashMap<>();
        this.lock = new ReentrantLock();
        this.musicId = 0;
        this.numDownloads = 0;
    }

    public ArrayList<String> pesquisarMusica(String etiqueta){
        ArrayList<String> musicas = new ArrayList<>();
        ArrayList<Long> temEtiqueta = new ArrayList<>();

        this.lock.lock();

        this.musicas.entrySet().stream().forEach(h -> {
            if (h.getValue().containsEtiqueta(etiqueta)) {
                temEtiqueta.add(h.getKey());
            }
        });
        temEtiqueta.stream().sorted().forEach(h -> this.musicas.get(h).lockMusica());

        this.lock.unlock();

        temEtiqueta.stream().sorted().forEach(h -> {
            musicas.add(this.musicas.get(h).dadosPesquisa());
            this.musicas.get(h).unlockMusica();
        });

        return musicas;
    }

    public String downloadOk(long id){
        String r = null;

        this.lock.lock();

        if (this.musicas.containsKey(id)){
            r = this.musicas.get(id).getTituloDownload();
        }

        this.lock.unlock();

        return r;
    }

    public long incMusicId(){

        long id;
        this.lock.lock();

        id = this.musicId++;

        this.lock.unlock();

        return id;
    }

    public void upload(Long id, String titulo, String interprete, String ano, List<String> tags, String extensao){

        this.lock.lock();

        this.musicas.put(id, new Musica(id, titulo, interprete, ano, tags, extensao));

        this.lock.unlock();
    }

    public void download(long id, Socket cs) throws IOException {

        String extensao;

        this.lock.lock();

        Musica m = this.musicas.get(id);

        m.incNumDownld();

        extensao = m.getExtensao();

        this.lock.unlock();

        getMusica(id, cs, extensao);

    }

    private void getMusica(long id, Socket cs, String extensao) throws IOException {
        File file = new File(this.pastaBD+id+extensao);   // tirar mais tarde mudar para txt
        long fileLength = file.length();
        long current = 0;
        int size = this.MAXSIZE;
        byte[] contents;

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));


        OutputStream os = cs.getOutputStream();

        while(current!=fileLength){

            if (fileLength - current >= size){
                current += size;
            }
            else {
                size = (int) (fileLength - current);
                current = fileLength;
            }

            contents = new byte[size];
            bis.read(contents, 0, size);
            os.write(contents);

            os.flush();

        }

        cs.shutdownInput();
        cs.shutdownOutput();
        cs.close();
    }
}
