package com.task.data.remote;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.task.App;
import com.task.data.remote.dto.NewsModel;
import com.task.data.remote.service.NewsService;
import com.task.utils.Constants;
import com.task.utils.L;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Call;

import static com.task.data.remote.ServiceError.NETWORK_ERROR;
import static com.task.utils.Constants.ERROR_UNDEFINED;
import static com.task.utils.NetworkUtils.isConnected;
import static com.task.utils.ObjectUtil.isNull;

/**
 * Created by AhmedEltaher on 5/12/2016
 */

public class RemoteRepository implements RemoteSource {
    private ServiceGenerator serviceGenerator;

    @Inject
    public RemoteRepository(ServiceGenerator serviceGenerator) {
        this.serviceGenerator = serviceGenerator;
    }

    @Override
    public ServiceResponse getNews() {
        if (!isConnected(App.getContext())) {
            return null;
        } else {
            NewsService newsService = serviceGenerator.createService(NewsService.class, Constants.BASE_URL);
            ServiceResponse serviceResponse = processCall(newsService.fetchNews(), false);
            return serviceResponse;
        }
    }

    @NonNull
    private ServiceResponse processCall(Call call, boolean isVoid) {
        if (!isConnected(App.getContext())) {
            return new ServiceResponse(new ServiceError());
        }
        try {
            retrofit2.Response response = call.execute();
            Gson gson = new Gson();
            L.json(NewsModel.class.getName(), gson.toJson(response.body()));
            if (isNull(response)) {
                return new ServiceResponse(new ServiceError(NETWORK_ERROR, ERROR_UNDEFINED));
            }
            int responseCode = response.code();
            if (response.isSuccessful()) {
                return new ServiceResponse(responseCode, isVoid ? null : response.body());
            } else {
                ServiceError ServiceError;
                ServiceError = new ServiceError(response.message(), responseCode);
                return new ServiceResponse(ServiceError);
            }
        } catch (IOException e) {
            return new ServiceResponse(new ServiceError(NETWORK_ERROR, ERROR_UNDEFINED));
        }
    }
}
