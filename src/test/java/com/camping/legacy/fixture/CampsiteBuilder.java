package com.camping.legacy.fixture;

import com.camping.legacy.domain.Campsite;

/**
 * Campsite 테스트 데이터 빌더
 */
public class CampsiteBuilder {

    private Long id = 1L;
    private String siteNumber = "A-1";
    private int maxPeople = 6;
    private String description = "테스트 캠프사이트";

    public static CampsiteBuilder aCampsite() {
        return new CampsiteBuilder();
    }

    public static CampsiteBuilder aLargeSite() {
        return new CampsiteBuilder()
                .withSiteNumber("A-1")
                .withMaxPeople(6);
    }

    public static CampsiteBuilder aSmallSite() {
        return new CampsiteBuilder()
                .withSiteNumber("B-1")
                .withMaxPeople(4);
    }

    public CampsiteBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public CampsiteBuilder withSiteNumber(String siteNumber) {
        this.siteNumber = siteNumber;
        return this;
    }

    public CampsiteBuilder withMaxPeople(int maxPeople) {
        this.maxPeople = maxPeople;
        return this;
    }

    public CampsiteBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public Campsite build() {
        Campsite campsite = new Campsite();
        campsite.setId(id);
        campsite.setSiteNumber(siteNumber);
        campsite.setMaxPeople(maxPeople);
        campsite.setDescription(description);
        return campsite;
    }
}
