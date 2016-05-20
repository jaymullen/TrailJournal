package com.jaymullen.TrailJournal.core;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 3/2/13
 * Time: 3:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class JournalItem implements Serializable {

    public JournalItem(String id, String name, String hits, String trailId, String cid) {
        this.id = id;
        this.name = name;
        this.hits = hits;
        this.trailId = trailId;
        this.cid = cid;
    }

    public String id;
    public String name;
    public String cid;
    public String trailId;
    public String hits;
}