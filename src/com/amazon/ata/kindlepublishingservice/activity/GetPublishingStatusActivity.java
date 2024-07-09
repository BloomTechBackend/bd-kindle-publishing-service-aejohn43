package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class GetPublishingStatusActivity {
    private PublishingStatusDao publishingStatusDao;
    @Inject
    public GetPublishingStatusActivity(PublishingStatusDao publishingStatusDao) {
        this.publishingStatusDao = publishingStatusDao;
    }

    public GetPublishingStatusResponse execute(GetPublishingStatusRequest publishingStatusRequest) {
        List<PublishingStatusItem> publishingStatusItemList = new ArrayList<>();
        publishingStatusItemList = publishingStatusDao.getPublishingStatus(publishingStatusRequest.getPublishingRecordId());
        List<PublishingStatusRecord> publishingStatusRecordsList = new ArrayList<>();

        for (PublishingStatusItem publishingStatusItem : publishingStatusItemList){
            publishingStatusRecordsList.add(new PublishingStatusRecord(
                    publishingStatusItem.getStatus().toString(),
                    publishingStatusItem.getStatusMessage(),
                    publishingStatusItem.getBookId()
            ));
        }
        return GetPublishingStatusResponse.builder()
                .withPublishingStatusHistory(publishingStatusRecordsList)
                .build();
    }
}
