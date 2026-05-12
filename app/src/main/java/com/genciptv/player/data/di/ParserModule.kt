package com.genciptv.player.data.di

import com.genciptv.player.data.source.epg.XmltvParser
import com.genciptv.player.data.source.epg.XmltvParserImpl
import com.genciptv.player.data.source.m3u.M3uParser
import com.genciptv.player.data.source.m3u.M3uParserImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ParserModule {

    @Binds @Singleton
    abstract fun bindM3uParser(impl: M3uParserImpl): M3uParser

    @Binds @Singleton
    abstract fun bindXmltvParser(impl: XmltvParserImpl): XmltvParser
}
