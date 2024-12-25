package Controller;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import Service.ServiceException;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class SocialMediaController {

    private final AccountService accountService;
    private final MessageService messageService;

    public SocialMediaController() {
        this.accountService = new AccountService();
        this.messageService = new MessageService();
    }

    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.post("/register", this::registerAccount);
        app.post("/login", this::loginAccount);
        app.post("/messages", this::createMessage);
        app.get("/messages", this::getAllMessages);
        app.get("/messages/{message_id}", this::getMessageById);
        app.delete("/messages/{message_id}", this::deleteMessageById);
        app.patch("/messages/{message_id}", this::updateMessageById);
        app.get("/accounts/{account_id}/messages", this::getMessagesByAccountId);
        return app;
    }

    private void registerAccount(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = mapper.readValue(ctx.body(), Account.class);
        try {
            Account registeredAccount = accountService.createAccount(account);
            ctx.json(mapper.writeValueAsString(registeredAccount));
        } catch (ServiceException e) {
            ctx.status(400);
        }
    }

    private void loginAccount(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = mapper.readValue(ctx.body(), Account.class);
        try {
            Optional<Account> loggedInAccount = accountService.validateLogin(account);
            if (loggedInAccount.isPresent()) {
                ctx.sessionAttribute("logged_in_account", loggedInAccount.get());
                ctx.json(loggedInAccount.get());
            } else {
                ctx.status(401);
            }
        } catch (ServiceException e) {
            ctx.status(401);
        }
    }

    private void createMessage(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message message = mapper.readValue(ctx.body(), Message.class);
        try {
            Optional<Account> account = accountService.getAccountById(message.getPosted_by());
            ctx.json(messageService.createMessage(message, account));
        } catch (ServiceException e) {
            ctx.status(400);
        }
    }

    private void getAllMessages(Context ctx) {
        ctx.json(messageService.getAllMessages());
    }

    private void getMessageById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
                ctx.json(message.get());
            } else {
                ctx.status(200).result("");
            }
        } catch (NumberFormatException e) {
            ctx.status(400);
        } catch (ServiceException e) {
            ctx.status(200).result("");
        }
    }

    private void deleteMessageById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
                messageService.deleteMessage(message.get());
                ctx.json(message.get());
            }
            ctx.status(200);
        } catch (ServiceException e) {
            ctx.status(200);
        }
    }

    private void updateMessageById(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message message = mapper.readValue(ctx.body(), Message.class);
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            message.setMessage_id(id);
            ctx.json(messageService.updateMessage(message));
        } catch (ServiceException e) {
            ctx.status(400);
        }
    }

    private void getMessagesByAccountId(Context ctx) {
        try {
            int accountId = Integer.parseInt(ctx.pathParam("account_id"));
            List<Message> messages = messageService.getMessagesByAccountId(accountId);
            ctx.json(messages);
        } catch (ServiceException e) {
            ctx.status(400);
        }
    }
}
