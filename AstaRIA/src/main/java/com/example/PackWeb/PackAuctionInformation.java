package com.example.PackWeb;

import com.example.Bean.ArticleBEAN;
import com.example.Bean.OfferBEAN;

import java.util.List;

public class PackAuctionInformation {
    private final List<ArticleBEAN> articles;
    private final List<OfferBEAN> offers;
    private final float actualOffer;
    private final float minimumRise;


    public PackAuctionInformation(List<ArticleBEAN> articles, List<OfferBEAN> offers, float minimumRise, float actualOffer) {
        this.articles = articles;
        this.offers = offers;
        this.minimumRise=minimumRise;
        this.actualOffer=actualOffer;
    }

    public float getActualOffer() {
        return actualOffer;
    }

    public float getMinimumRise() {
        return minimumRise;
    }

    public List<ArticleBEAN> getArticles() {
        return articles;
    }

    public List<OfferBEAN> getOffers() {
        return offers;
    }
}
