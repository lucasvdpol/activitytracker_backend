// package org.acme.notification;

// import static io.restassured.RestAssured.given;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.UUID;

// import org.acme.notification.service.FcmService;
// import org.acme.notification.service.FcmService.SendResult;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import io.quarkus.test.junit.QuarkusTest;
// import io.quarkus.test.junit.mockito.InjectMock;
// import io.restassured.http.ContentType;

// @QuarkusTest
// class NotificationResourceTest {

//     @InjectMock
//     FcmService fcmService;

//     private String accessToken;

//     @BeforeEach
//     void setUp() {
//         when(fcmService.send(any(), any(), any())).thenReturn(SendResult.SENT);

//         String email = "notif-" + UUID.randomUUID() + "@example.com";
//         String password = "password123";

//         given().contentType(ContentType.JSON)
//                 .body("""
//                         {"name":"Notif Tester","email":"%s","password":"%s"}
//                         """.formatted(email, password))
//                 .when().post("/auth/register")
//                 .then().statusCode(201);

//         accessToken = given().contentType(ContentType.JSON)
//                 .body("""
//                         {"email":"%s","password":"%s"}
//                         """.formatted(email, password))
//                 .when().post("/auth/login")
//                 .then().statusCode(200)
//                 .extract().path("accessToken");
//     }

//     @Test
//     void subscribeCreatesSubscription() {
//         String token = "fcm-token-" + UUID.randomUUID();

//         given().auth().oauth2(accessToken)
//                 .contentType(ContentType.JSON)
//                 .body("{\"token\":\"%s\"}".formatted(token))
//                 .when().post("/api/notifications/subscribe")
//                 .then().statusCode(204);
//     }

//     @Test
//     void subscribingSameTokenTwiceDoesNotDuplicate() {
//         String token = "fcm-token-" + UUID.randomUUID();
//         String body = "{\"token\":\"%s\"}".formatted(token);

//         given().auth().oauth2(accessToken)
//                 .contentType(ContentType.JSON)
//                 .body(body)
//                 .when().post("/api/notifications/subscribe")
//                 .then().statusCode(204);

//         given().auth().oauth2(accessToken)
//                 .contentType(ContentType.JSON)
//                 .body(body)
//                 .when().post("/api/notifications/subscribe")
//                 .then().statusCode(204);
//     }

//     @Test
//     void subscribeRequiresAuthentication() {
//         given().contentType(ContentType.JSON)
//                 .body("{\"token\":\"fcm-token-unauthenticated\"}")
//                 .when().post("/api/notifications/subscribe")
//                 .then().statusCode(401);
//     }

//     @Test
//     void sendInvokesFcmServiceForSubscribedToken() {
//         String token = "fcm-token-" + UUID.randomUUID();

//         given().auth().oauth2(accessToken)
//                 .contentType(ContentType.JSON)
//                 .body("{\"token\":\"%s\"}".formatted(token))
//                 .when().post("/api/notifications/subscribe")
//                 .then().statusCode(204);

//         given().auth().oauth2(accessToken)
//                 .contentType(ContentType.JSON)
//                 .body("{\"title\":\"Hello\",\"body\":\"World\"}")
//                 .when().post("/api/notifications/send")
//                 .then().statusCode(204);

//         verify(fcmService).send(eq(token), eq("Hello"), eq("World"));
//     }
// }
