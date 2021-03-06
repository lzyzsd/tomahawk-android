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
package org.tomahawk.tomahawk_android.utils;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

/**
 * A group of several {@link FakePreference}s
 */
public class FakePreferenceGroup {

    public static final int FAKEPREFERENCE_TYPE_AUTH = 0;

    public static final int FAKEPREFERENCE_TYPE_CHECKBOX = 1;

    public static final int FAKEPREFERENCE_TYPE_PLAIN = 2;

    public static final int FAKEPREFERENCE_TYPE_SPINNER = 3;

    public static final int FAKEPREFERENCE_TYPE_CONFIG = 4;

    private ArrayList<FakePreference> mFakePreferences = new ArrayList<FakePreference>();

    private String mHeader;

    /**
     * A {@link FakePreference} contains all information needed to provide the {@link
     * org.tomahawk.tomahawk_android.adapters.FakePreferencesAdapter} with the necessary values to
     * be displayed inside the {@link org.tomahawk.tomahawk_android.fragments.FakePreferenceFragment}'s
     * {@link android.widget.ListView}
     */
    public static class FakePreference {

        // this FakePreference's type (Dialog, Checkbox, Plain or Spinner)
        private int type;

        // the key to identify this FakePreference
        private int key;

        // the key to store preferences with
        private String storageKey;

        // if this FakePreference's type is FAKEPREFERENCE_TYPE_CHECKBOX or FAKEPREFERENCE_TYPE_AUTH
        // this contains the current state of this preference
        private boolean isEnabled;

        private String title;

        // short summary text to describe this FakePreference to the user
        private String summary;

        // drawable to show in grey, if isEnabled is false, otherwise colored
        private int drawableResId;

        // drawable to show in grey, if isEnabled is false, otherwise colored
        private Drawable drawable;

        /**
         * Construct a {@link FakePreference}
         */
        public FakePreference(int type, int key, String storageKey, String title, String summary) {
            this.type = type;
            this.key = key;
            this.storageKey = storageKey;
            this.title = title;
            this.summary = summary;
        }

        /**
         * Construct a {@link FakePreference}
         */
        public FakePreference(int type, int key, String title, String summary) {
            this.type = type;
            this.key = key;
            this.title = title;
            this.summary = summary;
        }

        /**
         * Construct a {@link FakePreference}
         */
        public FakePreference(int type, int key, String title, String summary, int drawableResId) {
            this.type = type;
            this.key = key;
            this.title = title;
            this.summary = summary;
            this.drawableResId = drawableResId;
        }

        /**
         * Construct a {@link FakePreference}
         */
        public FakePreference(int type, int key, boolean isEnabled, String title, String summary,
                Drawable drawable) {
            this.type = type;
            this.key = key;
            this.isEnabled = isEnabled;
            this.title = title;
            this.summary = summary;
            this.drawable = drawable;
        }

        public int getType() {
            return type;
        }

        public int getKey() {
            return key;
        }

        public String getStorageKey() {
            return storageKey;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public void setEnabled(boolean enabled) {
            this.isEnabled = enabled;
        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public int getDrawableResId() {
            return drawableResId;
        }

        public Drawable getDrawable() {
            return drawable;
        }
    }

    /**
     * Construct a {@link FakePreferenceGroup}
     *
     * @param header the header to be shown to the user
     */
    public FakePreferenceGroup(String header) {
        mHeader = header;
    }

    /**
     * Add a {@link FakePreference} to this {@link FakePreferenceGroup}
     */
    public void addFakePreference(FakePreference fakePreference) {
        mFakePreferences.add(fakePreference);
    }

    /**
     * @return an {@link ArrayList} of all {@link FakePreference}s
     */
    public ArrayList<FakePreference> getFakePreferences() {
        return mFakePreferences;
    }

    /**
     * Get the {@link FakePreference} with the given key
     */
    public FakePreference getFakePreferenceByKey(int key) {
        for (FakePreference fakePreference : mFakePreferences) {
            if (fakePreference.getKey() == key) {
                return fakePreference;
            }
        }
        return null;
    }

    /**
     * @return the header to be shown to the user
     */
    public String getHeader() {
        return mHeader;
    }

}
