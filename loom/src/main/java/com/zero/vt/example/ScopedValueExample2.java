package com.zero.vt.example;

import java.util.UUID;

/**
 * @author Zero.
 * <p> Created on 2025/7/1 15:35 </p>
 */
public class ScopedValueExample2 {
    private static final ScopedValue<String> USER_ID = ScopedValue.newInstance();
    public static void main(String[] args) {
        ScopedValue.where(USER_ID, UUID.randomUUID().toString())
                .run(()-> {
                    System.out.println("main, userId: " + USER_ID.get()); // main, userId: cc503702-79c0-45f1-a2fd-bdfefa24031a
                    processRequest();
                });
        System.out.println("main external, userId: " + USER_ID.orElse("Not set"));
    }

    private static void processRequest(){
        String userId = USER_ID.get();
        System.out.println("processRequest, userId:" + userId); // processRequest, userId:cc503702-79c0-45f1-a2fd-bdfefa24031a
        performDatabaseOperation();
    }

    private static void performDatabaseOperation(){
        String userId = USER_ID.get();
        System.out.println("performDatabaseOperation, userId:" + userId); // performDatabaseOperation, userId:cc503702-79c0-45f1-a2fd-bdfefa24031a
    }
}
