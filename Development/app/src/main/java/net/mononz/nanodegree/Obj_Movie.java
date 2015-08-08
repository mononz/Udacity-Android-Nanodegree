package net.mononz.nanodegree;

public class Obj_Movie {

    public int id;
    public int adult;
    public String backdrop_path;
    public String genre_ids;
    public String original_language;
    public String original_title;
    public String overview;
    public String release_date;
    public String poster_path;
    public double popularity;
    public String title;
    public int video;
    public double vote_average;
    public int vote_count;

    public Obj_Movie(int id, int adult, String backdrop_path, String genre_ids, String original_language, String original_title, String overview, String release_date, String poster_path, double popularity, String title, int video, double vote_average, int vote_count) {
        this.id = id;
        this.adult = adult;
        this.backdrop_path = backdrop_path;
        this.genre_ids = genre_ids;
        this.original_language = original_language;
        this.original_title = original_title;
        this.overview = overview;
        this.release_date = release_date;
        this.poster_path = poster_path;
        this.popularity = popularity;
        this.title = title;
        this.video = video;
        this.vote_average = vote_average;
        this.vote_count = vote_count;
    }
}