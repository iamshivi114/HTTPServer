import java.io.*;
import java.net.*;

public class HTTPServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("Server started on port 8080...");

            while(true) {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(() -> handleRequest(clientSocket));
                thread.start();
            }
        } catch(IOException err) {
            err.printStackTrace();
        }
    }
    
    private static void handleRequest(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();
    
            String requestLine = in.readLine();
            if (requestLine != null) {
                String[] requestParts = requestLine.split("\\s+");
                String method = requestParts[0];
                String path = requestParts[1];
                System.out.println("Method: " + method + ", Path: " + path);
                if (method.equals("GET")) {
                    handleGetRequest(out, path);
                } else if (method.equals("POST")) {
                    handlePostRequest(in, out, path);
                } else if (method.equals("PUT")) {
                    handlePutRequest(in, out, path);
                } else if (method.equals("DELETE")) {
                    handleDeleteRequest(out, path);
                } else if (method.equals("OPTIONS")) {
                    handleOptionsRequest(out);
                } else if (method.equals("HEAD")) {
                    handleHeadRequest(out, path);
                } else {
                    sendResponse(out, "HTTP/1.1 400 Bad Request", "Unsupported HTTP method");
                }
            }
            clientSocket.close();
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    private static void handleGetRequest(OutputStream out, String path) throws IOException {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            while((line = fileReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            fileReader.close();
            sendResponse(out, "HTTP/1.1 200 OK", content.toString());
        } else {
            sendResponse(out, "HTTP/1.1 404 Not Found", "File not found");
        }
    }

    private static void handlePostRequest(BufferedReader in, OutputStream out, String path) throws IOException {
        StringBuilder body = new StringBuilder();
        String line;
        while((line = in.readLine()) != null) {
            body.append(line).append("\n");
        }
        FileWriter fileWriter = new FileWriter(path, true);
        fileWriter.write(body.toString());
        fileWriter.close();
        
        sendResponse(out, "HTTP/1.1 200 OK", "File updated");
    }

    private static void handlePutRequest(BufferedReader in, OutputStream out, String path) throws IOException {
        FileWriter fileWriter = new FileWriter(path, false);
        String line;
        while((line = in.readLine()) != null) {
            fileWriter.write(line + "\n");
        }
        fileWriter.close();

        sendResponse(out, "HTTP/1.1 200 OK", "File updated");
    }

    private static void handleDeleteRequest(OutputStream out, String path) throws IOException {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            file.delete();

            sendResponse(out, "HTTP/1.1 200 OK", "File deleted");
        } else {
            sendResponse(out, "HTTP/1.1 404 Not Found", "File not found");
        }
    }

    private static void sendResponse(OutputStream out, String status, String message) throws IOException {
        String responseBody = "<html><body><h1>" + message + "</h1></body></html>";
        String response = status + "\r\n" + "Content-Type: text/html\r\n" + "Content-Length: " + responseBody.length() + "\r\n" + "\r\n" + responseBody;
        out.write(response.getBytes());
    }

    private static void handleOptionsRequest(OutputStream out) throws IOException {
        sendResponse(out, "HTTP/1.1 200 OK", "Allow: GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
    
    private static void handleHeadRequest(OutputStream out, String path) throws IOException {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            sendResponse(out, "HTTP/1.1 200 OK", "");
        } else {
            sendHTTPCatResponse(out, "HTTP/1.1 404 Not Found", "File not found", "https://http.cat/404");
        }
    }

    private static void sendHTTPCatResponse(OutputStream out, String status, String message, String imageURL) throws IOException {
        String responseBody = "<html><body><img src=\"" + imageURL + "\"><h1>" + message + "</h1></body></html>";
        String response = status + "\r\n" + "Content-Type: text/html\r\n" + "Content-Length: " + responseBody.length() + "\r\n" + "\r\n" + responseBody;
        out.write(response.getBytes());
    }    
}