/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.data.vector;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;

/**
 * Vector containing a user's ratings.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserRatingVector extends UserVector {
	private static final long serialVersionUID = 8742253275373260663L;

	public UserRatingVector(long user, Long2DoubleMap ratings) {
		super(user, ratings);
	}
	
	protected UserRatingVector(long user, long[] items, double[] values, int size) {
	    super(user, items, values, size);
	}
	
	/**
     * Real implementation of {@link #fromRatings(long,Collection)}, using a list
     * we are free to sort.
     * @param ratings
     * @return A vector containing the ratings of the list.
     */
    private static UserRatingVector userRatingVector(long userId, ArrayList<? extends Rating> ratings) {
        Rating rp = null;
        for (Rating r: ratings) {
            if (rp != null && Ratings.ITEM_TIME_COMPARATOR.compare(rp, r) > 0) {
                Collections.sort(ratings, Ratings.ITEM_TIME_COMPARATOR);
                break;
            }
            rp = r;
        }
        
        // collect the list of unique item IDs
        long[] items = new long[ratings.size()];
        double[] values = new double[ratings.size()];
        int li = -1;
        for (Rating r: ratings) {
            long iid = r.getItemId();
            if (li < 0 || items[li] != iid)
                li++;
            items[li] = iid;
            values[li] = r.getRating();
        }
        
        return new UserRatingVector(userId, items, values, li+1);
    }
    
    /**
     * Construct a rating vector that contains the ratings provided for each
     * item. If all ratings in <var>ratings</var> are by the same user, then
     * this will be a valid user rating vector. If multiple ratings are provided
     * for the same item, the one with the greatest timestamp is retained. Ties
     * are broken by preferring ratings which come later when iterating through
     * the collection.
     * 
     * @param userId The user ID.
     * @param ratings A collection of ratings (should all be by the same user)/
     * @return A sparse vector mapping item IDs to ratings.
     */
    public static UserRatingVector fromRatings(long userId, Collection<? extends Rating> ratings) {
        return userRatingVector(userId, new ArrayList<Rating>(ratings));
    }
    
    /**
     * Extract a user rating vector from a rating cursor.
     * 
     * @param userId The user ID.
     * @param ratings A cursor of ratings.
     * @return The user rating vector.
     * @see #fromRatings(long, Collection)
     */
    public static UserRatingVector fromRatings(long userId, Cursor<? extends Rating> ratings) {
        return userRatingVector(userId, Cursors.makeList(ratings));
    }
    
    public static UserRatingVector wrap(long user, long[] keys, double[] values) {
        return wrap(user, keys, values, keys.length);
    }
    
    /**
     * Create a new user rating vector from pre-existing arrays.
     * @see MutableSparseVector#wrap(long[], double[], int)
     */
    public static UserRatingVector wrap(long user, long[] keys, double[] values, int size) {
        if (values.length < size)
            throw new IllegalArgumentException("value array too short");
        if (!isSorted(keys, size))
            throw new IllegalArgumentException("item array not sorted");
        return new UserRatingVector(user, keys, values, size);
    }

}
