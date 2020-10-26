package service;

import dao.MusicDao;
import entity.Music;

import java.util.List;

public class MusicService {
    public List<Music> findMusic(){
        MusicDao musicDao = new MusicDao();
        List<Music> musicList = musicDao.findMusic();
        return musicList;
    }
}
