package me.szilard95.hnreader;

/**
 * Created by szilard95 on 11/24/17.
 */

enum CallStatus {
    OK {
        @Override
        public String toString() {
            return "Updated";
        }
    }, END {
        @Override
        public String toString() {
            return "End of HN :(";
        }
    }, ERROR {
        @Override
        public String toString() {
            return "Error while updating";
        }
    }
}
