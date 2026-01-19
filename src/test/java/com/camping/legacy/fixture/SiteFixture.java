package com.camping.legacy.fixture;

import com.camping.legacy.domain.Campsite;
import com.camping.legacy.repository.CampsiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SiteFixture {

    @Autowired
    private CampsiteRepository campsiteRepository;

    public Campsite createLargeSite(String siteNumber) {
        return campsiteRepository.save(new Campsite(siteNumber, "대형 사이트 - 전기 있음", 6));
    }

    public Campsite createSmallSite(String siteNumber) {
        return campsiteRepository.save(new Campsite(siteNumber, "소형 사이트 - 전기 있음", 4));
    }

    public void initializeDefaultSites() {
        createLargeSite("A-1");
        createLargeSite("A-2");
        createLargeSite("A-3");
        createSmallSite("B-1");
        createSmallSite("B-2");
        createSmallSite("B-3");
    }
}
