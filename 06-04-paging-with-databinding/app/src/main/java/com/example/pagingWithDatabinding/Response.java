package com.example.pagingWithDatabinding;

import java.util.List;

public class Response {
    public int count;
    public String previous;
    public String next;
    public List<Result> results;
}