package com.tienda.util;

import com.tienda.dao.AccessBinnacleDAO;
import com.tienda.model.AccessBinnacle;
import com.tienda.model.User;

import java.net.InetAddress;
import java.time.LocalDateTime;

public class SessionManager {

    private static User currentUser;
    private static Integer currentBinnacleId;
    private static AccessBinnacleDAO binnacleDAO;

    public static void init(AccessBinnacleDAO dao) {
        binnacleDAO = dao;
    }

    public static void startSession(User user) {
        if (binnacleDAO == null) {
            throw new IllegalStateException("SessionManager no inicializado. Llama a SessionManager.init(dao) al iniciar la app.");
        }
        try {
            currentUser = user;

            AccessBinnacle ab = new AccessBinnacle();
            ab.setUser(user);
            ab.setEntryDateTime(LocalDateTime.now());
            ab.setIp(getLocalIpAddress());

            int id = binnacleDAO.create(ab);
            currentBinnacleId = id;
        } catch (Exception e) {
            e.printStackTrace();
            // Decide si fallar el login o solo loguear la excepción; aquí sólo imprimimos
        }
    }

    public static void endSession() {
        if (binnacleDAO == null) return;
        if (currentBinnacleId == null) {
            clearSession();
            return;
        }
        try {
            binnacleDAO.updateDepartureTime(currentBinnacleId, LocalDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clearSession();
        }
    }

    public static void handleAppClose() {
        // Llamado por el onCloseRequest del Stage
        endSession();
    }

    private static void clearSession() {
        currentUser = null;
        currentBinnacleId = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Integer getCurrentBinnacleId() {
        return currentBinnacleId;
    }

    private static String getLocalIpAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }
}