package com.revosleap.bxplayer.utils.utils

import android.content.Context
import android.support.v4.content.AsyncTaskLoader

internal abstract class WrappedAsyncTaskLoader<D>
/**
 * Constructor of `WrappedAsyncTaskLoader`
 *
 * @param context The [Context] to use.
 */
(context: Context) : AsyncTaskLoader<D>(context) {

    private var mData: D? = null

    /**
     * {@inheritDoc}
     */
    override fun deliverResult(data: D?) {
        if (!isReset) {
            this.mData = data
            super.deliverResult(data)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onStartLoading() {
        super.onStartLoading()

        if (this.mData != null) {
            deliverResult(this.mData)
        } else if (takeContentChanged() || this.mData == null) {
            forceLoad()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onStopLoading() {
        super.onStopLoading()
        // Attempt to cancel the current load task if possible
        cancelLoad()
    }

    /**
     * {@inheritDoc}
     */
    override fun onReset() {
        super.onReset()
        // Ensure the loader is stopped
        onStopLoading()
        this.mData = null
    }
}