package com.amazonaws.sample;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.cognitoidentity.model.Credentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.json.JSONObject;


public class  Main extends Application{

    public static void main(String[] args) {
        launch(args);
    }


    private static void ListBuckets(Credentials credentails, Stage primarystage) {
        BasicSessionCredentials awsCreds = new BasicSessionCredentials(credentails.getAccessKeyId(), credentails.getSecretKey(), credentails.getSessionToken());
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
        StringBuilder bucketslist = new StringBuilder();

        bucketslist.append("===========Credentials Details.=========== \n");

        bucketslist.append("Accesskey = " + credentails.getAccessKeyId() + "\n");
        bucketslist.append("Secret = " + credentails.getSecretKey() + "\n");
        bucketslist.append("SessionToken = " + credentails.getSessionToken() + "\n");

        bucketslist.append("============Bucket Lists===========\n");



        for (Bucket bucket : s3Client.listBuckets()) {
            bucketslist.append(bucket.getName());
            bucketslist.append("\n");

            System.out.println(" - " + bucket.getName());
        }


        ShowMessage.display("Cognito -  Returned Credentials", bucketslist.toString());



    }
    @Override
    public void start(Stage primaryStage) {

        CognitoHelper helper = new CognitoHelper();
        Stage window;
        Button signup_button;
        Button signin_button;
        Button forgot_pswd_button;


        window = primaryStage;
        window.setTitle("re:Invent 2017 - Cognito Workshop");
        VBox vb = new VBox();
        vb.setPadding(new Insets(10, 50, 50, 50));
        vb.setSpacing(10);
        // Username field
        TextField Username = new TextField();
        Label username_label = new Label("Username:");
        HBox hbu = new HBox();
        hbu.getChildren().addAll(username_label,Username);
        hbu.setSpacing(10);
        // password field
        TextField Password = new PasswordField();
        Label password_label = new Label("Password:");
        HBox hbp = new HBox();
        hbp.getChildren().addAll(password_label,Password);
        hbp.setSpacing(10);

        signup_button = new Button("Sign-Up");
        vb.setPadding(new Insets(10, 50, 50, 50));
        vb.setSpacing(10);
        Label lbl = new Label("");
        Image image = new Image(getClass().getClassLoader().getResourceAsStream("reinvent.png"));
        lbl.setGraphic(new ImageView(image));
        lbl.setTextFill(Color.web("#0076a3"));
        lbl.setFont(Font.font("Amble CN", FontWeight.BOLD, 24));
        vb.getChildren().add(lbl);
        signup_button.setOnAction(e -> {
            boolean result = ConfirmBox.display("re:Invent 2017 - Cognito Workshop", "Sign-Up Form");
            System.out.println(result);
        });
        signin_button = new Button("Sign-In");
        Label auth_message = new Label("");
        signin_button.setOnAction((ActionEvent e) -> {
            String result = helper.ValidateUser(Username.getText(), Password.getText());
            if (result != null) {
                System.out.println("User is authenticated:" + result);
                auth_message.setText("User is authenticated");
                JSONObject payload = CognitoJWTParser.getPayload(result);
                String provider = payload.get("iss").toString().replace("https://", "");


                Credentials credentails = helper.GetCredentials(provider, result);


                ListBuckets(credentails, primaryStage);
            } else {
                System.out.println("Username / Password is invalid");
                auth_message.setText("Username / Password is invalid");
            }

        });
        forgot_pswd_button = new Button("Forgot Password?");
        forgot_pswd_button.setOnAction(e -> {
            boolean result = ForgotPassword.display("re:Invent 2017 - Cognito Workshop", "Forgot password");
            System.out.println(result);
        });
        signup_button.setMaxWidth(142);
        signin_button.setMaxWidth(142);
        forgot_pswd_button.setMaxWidth(142);


        Hyperlink hl = new Hyperlink("Cognito Hosted UI");
        hl.setTooltip(new Tooltip(helper.GetHostedSignInURL()));
        hl.setOnAction((ActionEvent event) -> {
            Hyperlink h = (Hyperlink) event.getTarget();
            String s = h.getTooltip().getText();
            this.getHostServices().showDocument(s);
            event.consume();
        });
/* StackPane layout = new StackPane(); */
        vb.getChildren().addAll(hbu, hbp, signin_button, signup_button, forgot_pswd_button, auth_message, hl);
        vb.setAlignment(Pos.CENTER);


        Scene scene = new Scene(vb, 400, 500);
        window.setScene(scene);
        window.show();
    }


}
