package com.camping.legacy.fixture;

import com.camping.legacy.domain.Campsite;

/**
 * Campsite 테스트 데이터 빌더
 */
public class CampsiteTestBuilder {

    private Long id = 1L;
    private String siteNumber = "A-1";
    private int maxPeople = 6;
    private String description = "테스트 캠프사이트";

    public static CampsiteTestBuilder aCampsite() {
        return new CampsiteTestBuilder();
    }

    public static CampsiteTestBuilder aLargeSite() {
        return new CampsiteTestBuilder()
                .withSiteNumber("A-1")
                .withMaxPeople(6);
    }

    public static CampsiteTestBuilder aSmallSite() {
        return new CampsiteTestBuilder()
                .withSiteNumber("B-1")
                .withMaxPeople(4);
    }

    public CampsiteTestBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public CampsiteTestBuilder withSiteNumber(String siteNumber) {
        this.siteNumber = siteNumber;
        return this;
    }

    public CampsiteTestBuilder withMaxPeople(int maxPeople) {
        this.maxPeople = maxPeople;
        return this;
    }

    public CampsiteTestBuilder withDescription(String description) {
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
