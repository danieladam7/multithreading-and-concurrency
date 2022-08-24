package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;

import java.util.ArrayList;
import java.util.Collection;

public class PublishConferenceBroadcast implements Broadcast {
    private Collection<Model> published;

    public PublishConferenceBroadcast(Collection<Model> published) {
        this.published = published;
    }

    public Collection<Model> getPublished() {
        return published;
    }
}
