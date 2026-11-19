package com.masilo.morabarabacollection;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import java.util.concurrent.atomic.AtomicBoolean;

public class AdManager {
    private static final String TAG = "AdManager";
    private static AdManager instance;
    private RewardedAd rewardedAd;
    private RewardedInterstitialAd rewardedInterstitialAd;
    private boolean isLoadingRewarded = false;
    private boolean isLoadingRewardedInterstitial = false;
    private boolean isInitialized = false;
    private ConsentInformation consentInformation;
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);

    // Production Ad Unit IDs
    private static final String REWARDED_AD_UNIT_ID = "ca-app-pub-7137219980782301/7951032429";
    public static final String BANNER_AD_UNIT_ID = "ca-app-pub-7137219980782301/2483716818";
    private static final String REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7137219980782301/9625927399";

    public interface AdCompletionListener {
        void onAdCompleted();
    }

    private AdManager() {}

    public static synchronized AdManager getInstance() {
        if (instance == null) {
            instance = new AdManager();
        }
        return instance;
    }

    public void init(Activity activity, Runnable onReady) {
        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(activity);
        consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                () -> {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            activity,
                            loadAndShowError -> {
                                if (loadAndShowError != null) {
                                    Log.w(TAG, String.format("%s: %s",
                                            loadAndShowError.getErrorCode(),
                                            loadAndShowError.getMessage()));
                                }

                                if (consentInformation.canRequestAds()) {
                                    initializeMobileAdsSdk(activity, onReady);
                                } else if (onReady != null) {
                                    // Even if we can't request ads yet, we should signal ready 
                                    // if initialization already happened or won't happen.
                                    onReady.run();
                                }
                            }
                    );
                },
                requestConsentError -> {
                    Log.w(TAG, String.format("%s: %s",
                            requestConsentError.getErrorCode(),
                            requestConsentError.getMessage()));
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAdsSdk(activity, onReady);
                    } else if (onReady != null) {
                        onReady.run();
                    }
                });
    }

    private void initializeMobileAdsSdk(Activity activity, Runnable onReady) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            // If already called, don't run onReady again if it was already signaled
            // Actually, we need to be careful here. If multiple activities call init, 
            // they each want their onReady called.
            if (isInitialized && onReady != null) {
                onReady.run();
            }
            return;
        }

        MobileAds.initialize(activity, initializationStatus -> {
            isInitialized = true;
            if (onReady != null) onReady.run();
        });
    }

    public void loadRewardedAd(Context context) {
        if (!isInitialized || isLoadingRewarded || rewardedAd != null) {
            return;
        }
        isLoadingRewarded = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(context, REWARDED_AD_UNIT_ID,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        rewardedAd = null;
                        isLoadingRewarded = false;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        isLoadingRewarded = false;
                    }
                });
    }

    public void loadRewardedInterstitialAd(Context context) {
        if (!isInitialized || isLoadingRewardedInterstitial || rewardedInterstitialAd != null) {
            return;
        }
        isLoadingRewardedInterstitial = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedInterstitialAd.load(context, REWARDED_INTERSTITIAL_AD_UNIT_ID,
                adRequest, new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        rewardedInterstitialAd = null;
                        isLoadingRewardedInterstitial = false;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedInterstitialAd ad) {
                        rewardedInterstitialAd = ad;
                        isLoadingRewardedInterstitial = false;
                    }
                });
    }

    public void showRewardedAd(Activity activity, OnUserEarnedRewardListener listener) {
        if (rewardedAd != null) {
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    rewardedAd = null;
                    loadRewardedAd(activity);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    rewardedAd = null;
                    loadRewardedAd(activity);
                }
            });
            rewardedAd.show(activity, listener);
        } else {
            Toast.makeText(activity, "Ad is still loading...", Toast.LENGTH_SHORT).show();
            loadRewardedAd(activity);
        }
    }

    public void showRewardedInterstitialAd(Activity activity, OnUserEarnedRewardListener rewardListener, AdCompletionListener completionListener) {
        if (rewardedInterstitialAd != null) {
            rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    rewardedInterstitialAd = null;
                    loadRewardedInterstitialAd(activity);
                    if (completionListener != null) completionListener.onAdCompleted();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    rewardedInterstitialAd = null;
                    loadRewardedInterstitialAd(activity);
                    if (completionListener != null) completionListener.onAdCompleted();
                }
            });
            rewardedInterstitialAd.show(activity, rewardListener);
        } else {
            Toast.makeText(activity, "Ad is still loading...", Toast.LENGTH_SHORT).show();
            loadRewardedInterstitialAd(activity);
            if (completionListener != null) completionListener.onAdCompleted();
        }
    }

    public boolean isRewardedLoaded() {
        return rewardedAd != null;
    }

    public boolean isRewardedInterstitialLoaded() {
        return rewardedInterstitialAd != null;
    }

    public AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public boolean isPrivacyOptionsRequired() {
        return consentInformation != null && 
               consentInformation.getPrivacyOptionsRequirementStatus() == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    public void showPrivacyOptionsForm(Activity activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, formError -> {
            if (formError != null) {
                Log.w(TAG, String.format("%s: %s", formError.getErrorCode(), formError.getMessage()));
            }
        });
    }
}
