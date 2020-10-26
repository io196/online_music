package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import entity.Music;
import util.DBUtils;

public class MusicDao {

    //查询全部歌曲
    public  List<Music> findMusic(){
        List<Music> musics = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement("select*from music");
            rs = ps.executeQuery();
            while(rs.next()) {
                Music music = new Music();
                music.setId(rs.getInt("id"));
                music.setTitle(rs.getString("title"));
                music.setSinger(rs.getString("singer"));
                music.setTime(rs.getDate("time"));
                music.setUrl(rs.getString("url"));
                music.setUserid(rs.getInt("userid"));
                musics.add(music);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            DBUtils.getClose(conn, ps, rs);
        }
        return musics;
    }

    //根据 id 查找音乐
    public Music findMusicById(int id){
        Music music = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement("select * from music where id=?");
            ps.setInt(1,id);
            rs = ps.executeQuery();
            if(rs.next()) {
                music = new Music();
                music.setId(rs.getInt("id"));
                music.setTitle(rs.getString("title"));
                music.setSinger(rs.getString("singer"));
                music.setTime(rs.getDate("time"));
                music.setUrl(rs.getString("url"));
                music.setUserid(rs.getInt("userid"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            DBUtils.getClose(conn, ps, rs);
        }
        return music;
    }

    //根据关键字查找歌单
    public List<Music> ifMusic(String str){ //str=歌名
        List<Music> musics = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement("select * from music where title like '%"+str+"%'");
            rs = ps.executeQuery();
            while(rs.next()) {
                Music music = new Music();
                music.setId(rs.getInt("id"));
                music.setTitle(rs.getString("title"));
                music.setSinger(rs.getString("singer"));
                music.setTime(rs.getDate("time"));
                music.setUrl(rs.getString("url"));
                music.setUserid(rs.getInt("userid"));
                musics.add(music);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            DBUtils.getClose(conn, ps, rs);
        }
        return musics;
    }


    //上传音乐
    public int insert(String title, String singer, String time, String url, int userid) {
        Connection conn = DBUtils.getConnection();
        PreparedStatement pst=null;
        int number = 0;
        try {
            pst=conn.prepareStatement("insert into music(title,singer,time,url,userid) values(?,?,?,?,?)");
            pst.setString(1,title);
            pst.setString(2,singer);
            pst.setString(3,time);
            pst.setString(4,url);
            pst.setInt(5,userid);
            number=pst.executeUpdate();
            return number;
        } catch (SQLException e) {
            e.printStackTrace();
        }finally
        {
            DBUtils.getClose(conn, pst, null);
        }
        return 0;
    }

    //删除歌曲
    public int deleteMusicById(int id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            String sql = "delete from music where id=?";
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1,id);
            int ret = preparedStatement.executeUpdate();
            if(ret == 1) {
    //同时删除中间表中的数据
    //1、看中间表是否有数据，如果有删除
                if(findLoveMusicOnDel(id)) {
                    int ret2 = removeLoveMusicOnDelete(id);
                    if(ret2 == 1){
                        return 1;
                    }
                } else {
    //如果没有找到，说明这首歌，没有被添加到喜欢的列表
                    return 1;
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtils.getClose(connection,preparedStatement,null);
        }
        return 0;
    }
//  看中间表是否有该id的音乐数据
    public boolean findLoveMusicOnDel(int id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String sql = "select * from lovemusic where music_id=?";
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1,id);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return true;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtils.getClose(connection,preparedStatement,null);
        }
        return false;
    }
    //当删除服务器上的音乐时，同时在我喜欢的列表的数据库中进行删除。
    public int removeLoveMusicOnDelete(int musicId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            String sql = "delete from lovemusic where music_id=?";
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1,musicId);
            int ret = preparedStatement.executeUpdate();
            if(ret == 1) {
                return ret;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtils.getClose(connection,preparedStatement,null);
        }
        return 0;
    }

    //    添加喜欢的音乐的时候，需要先判断该音乐是否存在
    public boolean findMusicByMusicId(int user_id,int musicID) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement("select * from lovemusic where music_id=? and user_id=?");
            ps.setInt(1,musicID);
            ps.setInt(2,user_id);
            rs = ps.executeQuery();
            if(rs.next()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            DBUtils.getClose(conn, ps, rs);
        }
        return false;
    }

    //添加音乐到喜欢的列表
    public boolean insertLoveMusic(int userId,int musicId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            String sql = "insert into lovemusic(user_id, music_id) VALUES (?,?)";
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1,userId);
            preparedStatement.setInt(2,musicId);
            int ret = preparedStatement.executeUpdate();
            if (ret == 1) {
                return true;
            }
            return false;
        }catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtils.getClose(connection,preparedStatement,null);
        }
        return false;
    }

    //移除当前用户选中的喜欢的音乐
    public int removeLoveMusic(int userId,int musicId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            String sql = "delete from lovemusic where user_id=? and music_id=?";
            connection = DBUtils.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1,userId);
            preparedStatement.setInt(2,musicId);
            int ret = preparedStatement.executeUpdate();
            if(ret == 1) {
                return ret;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtils.getClose(connection,preparedStatement,null);
        }
        return 0;
    }

    //查询用户喜欢的全部歌单
    public List<Music> findLoveMusic(int user_id){
        List<Music> musics = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement("select m.id as music_id,title,singer,time,url,userid from lovemusic lm,music m where lm.music_id=m.id and user_id=?");
            ps.setInt(1,user_id);
            rs = ps.executeQuery();
            while(rs.next()) {
                Music music = new Music();
                music.setId(rs.getInt("music_id"));
                music.setTitle(rs.getString("title"));
                music.setSinger(rs.getString("singer"));
                music.setTime(rs.getDate("time"));
                music.setUrl(rs.getString("url"));
                music.setUserid(rs.getInt("userid"));
                musics.add(music);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            DBUtils.getClose(conn, ps, rs);
        }
        return musics;
    }

    //根据关键字查询喜欢的歌单
    public List<Music> ifMusicLove(String str,int user_id){
        List<Music> musics = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            //ps = conn.prepareStatement("select*from music where title like '%"+str+"%'");
            ps = conn.prepareStatement("select m.id as music_id,title,singer,time,url,userid from lovemusic lm,music m where lm.music_id=m.id and user_id=? and title like '%"+str+"%'");
            ps.setInt(1,user_id);
            rs = ps.executeQuery();
            while(rs.next()) {
                Music music = new Music();
                music.setId(rs.getInt("music_id"));
                music.setTitle(rs.getString("title"));
                music.setSinger(rs.getString("singer"));
                music.setTime(rs.getDate("time"));
                music.setUrl(rs.getString("url"));
                music.setUserid(rs.getInt("userid"));
                musics.add(music);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            DBUtils.getClose(conn, ps, rs);
        }
        return musics;
    }




    //测试
    public static void main(String[] args) {
//        List<Music> musicList = findMusic();
//        System.out.println(musicList);
//        Music music = findMusicById(1);
//        List<Music> musicList = ifMusic("叹");
//        System.out.println(musicList);
//        int ret = Insert("锤子","ys","2020-07-23","music\\锤子",1);
//        System.out.println(ret);
    }
}
