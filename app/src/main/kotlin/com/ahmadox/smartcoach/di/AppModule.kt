package com.ahmadox.smartcoach.di

import android.content.Context
import com.ahmadox.smartcoach.llm.LocalLLMManager
import com.ahmadox.smartcoach.strategy.RuleBasedStrategyEngine
import com.ahmadox.smartcoach.vision.GameVisionAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideGameVisionAnalyzer(): GameVisionAnalyzer {
        return GameVisionAnalyzer()
    }
    
    @Provides
    @Singleton
    fun provideRuleBasedStrategyEngine(): RuleBasedStrategyEngine {
        return RuleBasedStrategyEngine()
    }
    
    @Provides
    @Singleton
    fun provideLocalLLMManager(@ApplicationContext context: Context): LocalLLMManager {
        return LocalLLMManager(context)
    }
}