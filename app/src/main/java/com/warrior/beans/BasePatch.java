package com.warrior.beans;

import java.io.Serializable;

/**
 * Created by Jamie
 */

public class BasePatch implements Serializable {
    public int ecode;
    public String emsg;
    public PatchInfo data;
}
