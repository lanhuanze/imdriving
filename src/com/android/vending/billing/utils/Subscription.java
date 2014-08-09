package com.android.vending.billing.utils;

/**
 * Created by lan on 8/9/14.
 */
public class Subscription {
    public static enum Type {
        NONE, MONTHLY, YEARLY;
    }

    public static enum Status {
        NONE, IN_PROGRESS, DONE, REFUND;
    }
}
