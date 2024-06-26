package com.nashss.se.booktrackerservice.lambda;

import com.nashss.se.booktrackerservice.activity.requests.SearchBooklistsRequest;
import com.nashss.se.booktrackerservice.activity.results.SearchBooklistsResult;

import com.amazonaws.services.lambda.runtime.Context;

import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SearchBooklistsLambda
        extends LambdaActivityRunner<SearchBooklistsRequest, SearchBooklistsResult>
        implements RequestHandler<LambdaRequest<SearchBooklistsRequest>, LambdaResponse> {

    private final Logger log = LogManager.getLogger();

    @Override
    public LambdaResponse handleRequest(LambdaRequest<SearchBooklistsRequest> input, Context context) {
        log.info("handleRequest:");
        return super.runActivity(
            () -> input.fromQuery(query ->
                    SearchBooklistsRequest.builder()
                            .withCriteria(query.get("q"))
                            .build()),
            (request, serviceComponent) ->
                    serviceComponent.provideSearchBooklistsActivity().handleRequest(request)
        );
    }
}
