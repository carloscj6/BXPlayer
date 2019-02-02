package com.revosleap.bxplayer.utils.utils


import android.content.Context
import android.util.Log

import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.GenericLoaderFactory
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.stream.StreamModelLoader
import com.bumptech.glide.module.GlideModule

import java.io.IOException
import java.io.InputStream

class GlideLoader : GlideModule {


    override fun applyOptions(context: Context, builder: GlideBuilder) {

    }

    override fun registerComponents(context: Context, glide: Glide) {
        glide.register(InputStream::class.java, InputStream::class.java, PassthroughStreamLoader.Factory())
    }

    class PassthroughStreamLoader : StreamModelLoader<InputStream> {
        override fun getResourceFetcher(model: InputStream, width: Int, height: Int): DataFetcher<InputStream> {
            return object : DataFetcher<InputStream> {
                @Throws(Exception::class)
                override fun loadData(priority: Priority): InputStream {
                    return model
                }

                override fun cleanup() {
                    try {
                        model.close()
                    } catch (e: IOException) {
                        Log.w("PassthroughDataFetcher", "Cannot clean up after stream", e)
                    }

                }

                override fun getId(): String {
                    return System.currentTimeMillis().toString() // There's no way to have a meaningful value here,
                    // which means that caching of straight-loaded InputStreams is not possible.
                }

                override fun cancel() {
                    // do nothing
                }
            }
        }

         class Factory : ModelLoaderFactory<InputStream, InputStream> {
            override fun build(context: Context, factories: GenericLoaderFactory): ModelLoader<InputStream, InputStream> {
                return PassthroughStreamLoader()
            }

            override fun teardown() {
                // nothing to do
            }
        }
    }
}
