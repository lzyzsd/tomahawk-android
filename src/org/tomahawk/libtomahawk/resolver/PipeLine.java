/* == This file is part of Tomahawk Player - <http://tomahawk-player.org> ===
 *
 *   Copyright 2013, Enno Gottschalk <mrmaffen@googlemail.com>
 *
 *   Tomahawk is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Tomahawk is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Tomahawk. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tomahawk.libtomahawk.resolver;

import org.tomahawk.libtomahawk.resolver.spotify.SpotifyResolver;
import org.tomahawk.libtomahawk.utils.TomahawkUtils;
import org.tomahawk.tomahawk_android.TomahawkApp;
import org.tomahawk.tomahawk_android.dialogs.ResolverConfigDialog;
import org.tomahawk.tomahawk_android.utils.ThreadManager;
import org.tomahawk.tomahawk_android.utils.TomahawkRunnable;

import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link PipeLine} is being used to provide all the resolving functionality. All {@link
 * Resolver}s are stored and invoked here. Callbacks which report the found {@link Result}s are also
 * included in this class.
 */
public class PipeLine {

    private static PipeLine instance;

    public static final int RESOLVER_ID_USERCOLLECTION = 0;

    public static final int RESOLVER_ID_JAMENDO = 100;

    public static final int RESOLVER_ID_OFFICIALFM = 101;

    public static final int RESOLVER_ID_EXFM = 102;

    public static final int RESOLVER_ID_SOUNDCLOUD = 103;

    public static final int RESOLVER_ID_BEATSMUSIC = 104;

    public static final int RESOLVER_ID_BEETS = 105;

    public static final int RESOLVER_ID_RDIO = 106;

    public static final int RESOLVER_ID_SPOTIFY = 200;

    public static final int PIPELINE_SEARCHTYPE_TRACKS = 0;

    public static final int PIPELINE_SEARCHTYPE_ARTISTS = 1;

    public static final int PIPELINE_SEARCHTYPE_ALBUMS = 2;

    public static final String PIPELINE_RESULTSREPORTED
            = "org.tomahawk.tomahawk_android.pipeline_resultsreported";

    public static final String PIPELINE_URLTRANSLATIONREPORTED
            = "org.tomahawk.tomahawk_android.pipeline_urltranslationreported";

    public static final String PIPELINE_URLTRANSLATIONREPORTED_URL
            = "org.tomahawk.tomahawk_android.pipeline_urltranslationreported_url";

    public static final String PIPELINE_URLTRANSLATIONREPORTED_RESULTKEY
            = "org.tomahawk.tomahawk_android.pipeline_urltranslationreported_resultkey";

    public static final String PIPELINE_RESULTSREPORTED_QUERYKEY
            = "org.tomahawk.tomahawk_android.pipeline_resultsreported_querykey";

    private static final float MINSCORE = 0.5F;

    private boolean mInitialized;

    private ArrayList<Resolver> mResolvers = new ArrayList<Resolver>();

    private ConcurrentHashMap<String, Query> mWaitingQueries
            = new ConcurrentHashMap<String, Query>();

    private boolean mAllResolversAdded;

    private PipeLine() {
    }

    public static PipeLine getInstance() {
        if (instance == null) {
            synchronized (PipeLine.class) {
                if (instance == null) {
                    instance = new PipeLine();
                }
            }
        }
        return instance;
    }

    public void ensureInit() {
        if (!mInitialized) {
            mInitialized = true;
            mResolvers.add(new DataBaseResolver(PipeLine.RESOLVER_ID_USERCOLLECTION));
            ScriptResolver scriptResolver = new ScriptResolver(PipeLine.RESOLVER_ID_BEATSMUSIC,
                    "js/beatsmusic/content");
            mResolvers.add(scriptResolver);
            scriptResolver = new ScriptResolver(PipeLine.RESOLVER_ID_RDIO,
                    "js/rdio/content");
            mResolvers.add(scriptResolver);
            scriptResolver = new ScriptResolver(PipeLine.RESOLVER_ID_JAMENDO,
                    "js/jamendo/content");
            mResolvers.add(scriptResolver);
            scriptResolver = new ScriptResolver(PipeLine.RESOLVER_ID_OFFICIALFM,
                    "js/official.fm/content");
            mResolvers.add(scriptResolver);
            scriptResolver = new ScriptResolver(PipeLine.RESOLVER_ID_EXFM,
                    "js/exfm/content");
            mResolvers.add(scriptResolver);
            scriptResolver = new ScriptResolver(PipeLine.RESOLVER_ID_SOUNDCLOUD,
                    "js/soundcloud/content");
            mResolvers.add(scriptResolver);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                scriptResolver = new ScriptResolver(PipeLine.RESOLVER_ID_BEETS,
                        "js/beets/content");
                mResolvers.add(scriptResolver);
            }
            SpotifyResolver spotifyResolver = new SpotifyResolver(PipeLine.RESOLVER_ID_SPOTIFY);
            mResolvers.add(spotifyResolver);
            setAllResolversAdded(true);
        }
    }

    /**
     * Get the {@link Resolver} with the given id, null if not found
     */
    public Resolver getResolver(int id) {
        for (Resolver resolver : mResolvers) {
            if (resolver.getId() == id) {
                return resolver;
            }
        }
        return null;
    }

    /**
     * Get the ArrayList of all {@link org.tomahawk.libtomahawk.resolver.ScriptResolver}s
     */
    public ArrayList<ScriptResolver> getScriptResolvers() {
        ArrayList<ScriptResolver> scriptResolvers = new ArrayList<ScriptResolver>();
        for (Resolver resolver : mResolvers) {
            if (resolver instanceof ScriptResolver) {
                scriptResolvers.add((ScriptResolver) resolver);
            }
        }
        return scriptResolvers;
    }

    /**
     * This will invoke every {@link Resolver} to resolve the given fullTextQuery. If there already
     * is a {@link Query} with the same fullTextQuery, the old resultList will be reported.
     */
    public String resolve(String fullTextQuery) {
        return resolve(fullTextQuery, false);
    }

    /**
     * This will invoke every {@link Resolver} to resolve the given fullTextQuery. If there already
     * is a {@link Query} with the same fullTextQuery, the old resultList will be reported.
     */
    public String resolve(String fullTextQuery, boolean forceOnlyLocal) {
        if (fullTextQuery != null && !TextUtils.isEmpty(fullTextQuery)) {
            Query q = Query.get(fullTextQuery, forceOnlyLocal);
            resolve(q, forceOnlyLocal);
            return q.getCacheKey();
        }
        return null;
    }

    /**
     * This will invoke every {@link Resolver} to resolve the given {@link
     * org.tomahawk.libtomahawk.collection.Track}/{@link org.tomahawk.libtomahawk.collection.Artist}/{@link
     * org.tomahawk.libtomahawk.collection.Album}. If there already is a {@link Query} with the same
     * {@link org.tomahawk.libtomahawk.collection.Track}/{@link org.tomahawk.libtomahawk.collection.Artist}/{@link
     * org.tomahawk.libtomahawk.collection.Album}, the old resultList will be reported.
     */
    public String resolve(String trackName, String albumName, String artistName) {
        if (trackName != null && !TextUtils.isEmpty(trackName)) {
            Query q = Query.get(trackName, albumName, artistName, false);
            return resolve(q);
        }
        return null;
    }

    /**
     * This will invoke every {@link Resolver} to resolve the given {@link Query}.
     */
    public String resolve(Query q) {
        return resolve(q, false);
    }

    /**
     * This will invoke every {@link Resolver} to resolve the given {@link Query}.
     */
    public String resolve(final Query q, final boolean forceOnlyLocal) {
        final TomahawkRunnable r = new TomahawkRunnable(TomahawkRunnable.PRIORITY_IS_RESOLVING) {
            @Override
            public void run() {
                if (!forceOnlyLocal && q.isSolved()) {
                    sendResultsReportBroadcast(q.getCacheKey());
                } else {
                    if (!isEveryResolverReady()) {
                        if (!mWaitingQueries.containsKey(q.getCacheKey())) {
                            mWaitingQueries.put(q.getCacheKey(), q);
                        }
                    } else {
                        for (final Resolver resolver : mResolvers) {
                            if (shouldResolve(resolver, q, forceOnlyLocal)) {
                                resolver.resolve(q);
                            }
                        }
                    }
                }
            }
        };
        ThreadManager.getInstance().execute(r, q);
        return q.getCacheKey();
    }

    /**
     * Method to determine if a given Resolver should resolve the query or not
     */
    public boolean shouldResolve(Resolver resolver, Query q, boolean forceOnlyLocal) {
        if (!forceOnlyLocal && !q.isOnlyLocal()) {
            if (resolver instanceof ScriptResolver) {
                ScriptResolver scriptResolver = ((ScriptResolver) resolver);
                if (scriptResolver.isEnabled()) {
                    if (scriptResolver.getId() != RESOLVER_ID_BEATSMUSIC) {
                        return true;
                    } else {
                        boolean configured = true;
                        Map<String, Object> config = scriptResolver.getConfig();
                        if (config != null && scriptResolver.getConfigUi() != null
                                && scriptResolver.getConfigUi().fields != null) {
                            for (ScriptResolverConfigUiField field : scriptResolver
                                    .getConfigUi().fields) {
                                if (ResolverConfigDialog.PROPERTY_TEXT.equals(field.property)
                                        && TextUtils.isEmpty((String) config.get(field.name))) {
                                    configured = false;
                                }
                            }
                        }
                        if (configured) {
                            return true;
                        }
                    }
                }
            } else {
                return true;
            }
        } else if (resolver instanceof DataBaseResolver) {
            return true;
        }
        return false;
    }

    /**
     * Resolve the given ArrayList of {@link org.tomahawk.libtomahawk.resolver.Query}s and return a
     * HashSet containing all query ids
     */
    public HashSet<String> resolve(ArrayList<Query> queries) {
        return resolve(queries, false);
    }

    /**
     * Resolve the given ArrayList of {@link org.tomahawk.libtomahawk.resolver.Query}s and return a
     * HashSet containing all query keys
     */
    public HashSet<String> resolve(ArrayList<Query> queries, boolean forceOnlyLocal) {
        HashSet<String> queryKeys = new HashSet<String>();
        if (queries != null) {
            for (Query query : queries) {
                if (forceOnlyLocal || !query.isSolved()) {
                    queryKeys.add(resolve(query, forceOnlyLocal));
                }
            }
        }
        return queryKeys;
    }

    /**
     * Send a broadcast containing the key of the resolved {@link org.tomahawk.libtomahawk.resolver.Result}.
     */
    public void sendUrlTranslationReportBroadcast(String resultKey, String url) {
        Intent reportIntent = new Intent(PIPELINE_URLTRANSLATIONREPORTED);
        reportIntent.putExtra(PIPELINE_URLTRANSLATIONREPORTED_RESULTKEY, resultKey);
        reportIntent.putExtra(PIPELINE_URLTRANSLATIONREPORTED_URL, url);
        TomahawkApp.getContext().sendBroadcast(reportIntent);
    }

    /**
     * Send a broadcast containing the key of the resolved {@link Query}.
     */
    private void sendResultsReportBroadcast(String queryKey) {
        Intent reportIntent = new Intent(PIPELINE_RESULTSREPORTED);
        reportIntent.putExtra(PIPELINE_RESULTSREPORTED_QUERYKEY, queryKey);
        TomahawkApp.getContext().sendBroadcast(reportIntent);
    }

    /**
     * If the {@link ScriptResolver} has resolved the {@link Query}, this method will be called.
     * This method will then calculate a score and assign it to every {@link Result}. If the score
     * is higher than MINSCORE the {@link Result} is added to the output resultList.
     *
     * @param queryKey the {@link Query}'s key
     * @param results  the unfiltered {@link ArrayList} of {@link Result}s
     */
    public void reportResults(final String queryKey, final ArrayList<Result> results,
            final int resolverId) {
        int priority;
        if (resolverId == RESOLVER_ID_EXFM) {
            priority = TomahawkRunnable.PRIORITY_IS_REPORTING_WITH_HEADERREQUEST;
        } else if (resolverId == RESOLVER_ID_USERCOLLECTION) {
            priority = TomahawkRunnable.PRIORITY_IS_REPORTING_LOCALSOURCE;
        } else if (resolverId == RESOLVER_ID_SPOTIFY) {
            priority = TomahawkRunnable.PRIORITY_IS_REPORTING_SUBSCRIPTION;
        } else {
            priority = TomahawkRunnable.PRIORITY_IS_REPORTING;
        }
        ThreadManager.getInstance().execute(
                new TomahawkRunnable(priority) {
                    @Override
                    public void run() {
                        ArrayList<Result> cleanTrackResults = new ArrayList<Result>();
                        ArrayList<Result> cleanAlbumResults = new ArrayList<Result>();
                        ArrayList<Result> cleanArtistResults = new ArrayList<Result>();
                        Query q = Query.getQueryByKey(queryKey);
                        if (q != null) {
                            for (Result r : results) {
                                if (r != null) {
                                    r.setTrackScore(q.howSimilar(r, PIPELINE_SEARCHTYPE_TRACKS));
                                    if (r.getTrackScore() >= MINSCORE
                                            && !cleanTrackResults.contains(r)) {
                                        if (resolverId != RESOLVER_ID_EXFM
                                                || TomahawkUtils.httpHeaderRequest(r.getPath())) {
                                            r.setType(Result.RESULT_TYPE_TRACK);
                                            cleanTrackResults.add(r);
                                        }
                                    }
                                    if (q.isFullTextQuery()) {
                                        r.setAlbumScore(
                                                q.howSimilar(r, PIPELINE_SEARCHTYPE_ALBUMS));
                                        if (r.getAlbumScore() >= MINSCORE
                                                && !cleanAlbumResults.contains(r)) {
                                            r.setType(Result.RESULT_TYPE_ALBUM);
                                            cleanAlbumResults.add(r);
                                        }
                                        r.setArtistScore(
                                                q.howSimilar(r, PIPELINE_SEARCHTYPE_ARTISTS));
                                        if (r.getArtistScore() >= MINSCORE
                                                && !cleanArtistResults.contains(r)) {
                                            r.setType(Result.RESULT_TYPE_ARTIST);
                                            cleanArtistResults.add(r);
                                        }
                                    }
                                }
                            }
                            q.addArtistResults(cleanArtistResults);
                            q.addAlbumResults(cleanAlbumResults);
                            q.addTrackResults(cleanTrackResults);
                            sendResultsReportBroadcast(q.getCacheKey());
                            if (q.isSolved()) {
                                ThreadManager.getInstance().stop(q);
                            }
                        }
                    }
                }
        );
    }

    /**
     * @return true if one or more {@link ScriptResolver}s are currently resolving. False otherwise
     */
    public boolean isResolving() {
        for (Resolver resolver : mResolvers) {
            if (resolver.isResolving() || !resolver.isReady()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return whether or not every Resolver in this PipeLine is ready to resolve queries
     */
    public boolean isEveryResolverReady() {
        if (!mAllResolversAdded) {
            return false;
        }
        for (Resolver r : mResolvers) {
            if (!r.isReady()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Callback method, which is being called by Resolvers as soon as they are ready
     */
    public void onResolverReady() {
        if (isEveryResolverReady()) {
            for (Query query : mWaitingQueries.values()) {
                mWaitingQueries.remove(query.getCacheKey());
                resolve(query);
            }
        }
    }

    public void setAllResolversAdded(boolean allResolversAdded) {
        mAllResolversAdded = allResolversAdded;
    }
}
