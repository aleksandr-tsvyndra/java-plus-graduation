package ru.practicum.recomm.client;

import com.google.common.collect.Lists;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.recommendations.messages.InteractionsCountRequest;
import ru.practicum.recommendations.messages.RecommendedEvent;
import ru.practicum.recommendations.messages.SimilarEventsRequest;
import ru.practicum.recommendations.messages.UserPredictionsRequest;
import ru.practicum.recommendations.services.RecommendationsControllerGrpc;

import java.util.List;

@Component
public class AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzer;

    public List<RecommendedEvent> getRecommendationsForUser(UserPredictionsRequest request) {
        return Lists.newArrayList(analyzer.getRecommendationsForUser(request));
    }

    public List<RecommendedEvent> getSimilarEvent(SimilarEventsRequest request) {
        return Lists.newArrayList(analyzer.getSimilarEvents(request));
    }

    public List<RecommendedEvent> getInteractionsCount(InteractionsCountRequest request) {
        return Lists.newArrayList(analyzer.getInteractionsCount(request));
    }
}