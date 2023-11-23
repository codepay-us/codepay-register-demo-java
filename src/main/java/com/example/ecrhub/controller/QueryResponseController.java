package com.example.ecrhub.controller;

import cn.hutool.core.util.StrUtil;
import com.example.ecrhub.constant.CommonConstant;
import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.manager.PurchaseManager;
import com.example.ecrhub.manager.SceneManager;
import com.example.ecrhub.pojo.ECRHubClientPo;
import com.example.ecrhub.util.JSONFormatUtil;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import com.wiseasy.ecr.hub.sdk.model.request.QueryRequest;
import com.wiseasy.ecr.hub.sdk.model.response.PurchaseResponse;
import com.wiseasy.ecr.hub.sdk.model.response.QueryResponse;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.LinkedHashMap;

/**
 * @author: yanzx
 * @date: 2023/10/12 16:14
 * @description:
 */
public class QueryResponseController {

    public Button queryButton;
    @FXML
    private Button returnButton;
    public TextField trans_amount;
    public TextField merchant_order_no;
    public TextArea response_info;
    private Task<String> task = null;
    public ChoiceBox<String> terminalBox;
    @FXML
    private Label wait_label;
    @FXML
    private ProgressIndicator progress_indicator;

    public void initialize() {

        QueryResponse queryResponse = PurchaseManager.getInstance().getQueryResponse();
        if (queryResponse != null){
            trans_amount.setText(queryResponse.getOrder_amount());
            merchant_order_no.setText(queryResponse.getMerchant_order_no());
            response_info.setText(JSONFormatUtil.formatJson(queryResponse));
        }else {
            return;
        }
    }


    @FXML
    private void queryReturnButtonAction(ActionEvent event) {
        PurchaseManager.getInstance().setQueryResponse(null);
        SceneManager.getInstance().loadScene("shopping", "/com/example/ecrhub/fxml/shopping.fxml");
        SceneManager.getInstance().switchScene("shopping");
    }

    @FXML
    private void handleQueryButtonAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        String merchantOrderNo = merchant_order_no.getText();
        if (StrUtil.isEmpty(merchantOrderNo)) {
            alert.setContentText("Please enter trans merchant_order_no");
            alert.showAndWait();
            return;
        }
        task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                PurchaseManager.getInstance().setQueryResponse(Query());
                return "success";
            }
        };
        task.setOnSucceeded(success -> {
            SceneManager.getInstance().loadScene("queryResponse", "/com/example/ecrhub/fxml/queryResponse.fxml");
            SceneManager.getInstance().switchScene("queryResponse");
        });

        task.setOnFailed(fail -> {
            alert.setContentText("connect error!");
            alert.showAndWait();
        });

        Thread thread = new Thread(task);
        thread.start();
    }

    private QueryResponse Query() throws Exception {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        // 设备选择
        ECRHubClient client;
        if (1 == instance.getConnectType()) {
            client = instance.getClient();
        } else {
            LinkedHashMap<String, ECRHubClientPo> client_list = instance.getClient_list();
            client = client_list.get(terminalBox.getValue()).getClient();
        }
        QueryRequest request = new QueryRequest();
        request.setApp_id(CommonConstant.APP_ID);
        request.setMerchant_order_no(merchant_order_no.getText());
        System.out.println("Query request:" + request);
        QueryResponse queryResponse = client.execute(request);
        System.out.println("Query response:" + queryResponse);
        return queryResponse;
    }
}
