package net.creeperhost.minetogether.gui.chat.ingame;

import net.creeperhost.minetogether.CreeperHost;
import net.creeperhost.minetogether.chat.ChatHandler;
import net.creeperhost.minetogether.gui.GuiGDPR;
import net.creeperhost.minetogether.gui.chat.GuiChatFriend;
import net.creeperhost.minetogether.gui.chat.GuiMTChat;
import net.creeperhost.minetogether.gui.chat.GuiTextFieldLockable;
import net.creeperhost.minetogether.gui.chat.TimestampComponentString;
import net.creeperhost.minetogether.gui.element.DropdownButton;
import net.creeperhost.minetogether.gui.element.GuiButtonPair;
import net.creeperhost.minetogether.paul.Callbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiChatOurs extends ChatScreen
{
    private DropdownButton<GuiMTChat.Menu> menuDropdownButton;
    private String activeDropdown;
    private GuiButtonPair switchButton;
    private String presetString;
    private boolean sleep;
    private boolean disabledDueToBadwords;
    Minecraft mc = Minecraft.getInstance();

    public static boolean isBase()
    {
        NewChatGui chat = Minecraft.getInstance().ingameGUI.getChatGUI();
        return !CreeperHost.instance.gdpr.hasAcceptedGDPR() || !(chat instanceof GuiNewChatOurs) || ((GuiNewChatOurs) chat).isBase();
    }

    @Override
    protected void renderComponentHoverEffect(ITextComponent component, int x, int y)
    {
//        if (component != null && component.getStyle().getHoverEvent() != null)
//        {
//            HoverEvent event = component.getStyle().getHoverEvent();
//            if (event.getAction() == CreeperHost.instance.TIMESTAMP)
//            {
//                List<ITextComponent> siblings = ((GuiNewChatOurs) mc.ingameGUI.getChatGUI()).getBaseChatComponent(x, y).getSiblings();
//                for(ITextComponent sibling: siblings) {
//                    if (sibling instanceof TimestampComponentString)
//                    {
//                        ((TimestampComponentString)sibling).setActive();
//                    }
//                }
//            }
//        }
        super.renderComponentHoverEffect(component, x, y);
    }

    public void processBadwords()
    {
        if (ChatHandler.badwordsFormat == null)
            return;
        String text = inputField.getText().replaceAll(ChatHandler.badwordsFormat, "");
        boolean veryNaughty = false;
        if (ChatHandler.badwords != null)
        {
            for (String bad : ChatHandler.badwords)
            {
                if (bad.startsWith("(") && bad.endsWith(")"))
                {
                    if (text.matches(bad))
                    {
                        veryNaughty = true;
                        break;
                    }
                }
                if (text.toLowerCase().contains(bad.toLowerCase()))
                {
                    veryNaughty = true;
                    break;
                }
            }
        }

        if ((Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) && ((GuiNewChatOurs) mc.ingameGUI.getChatGUI()).isBase())
            veryNaughty = false;

        if (veryNaughty)
        {
            ((GuiTextFieldLockable)inputField).setDisabled("Cannot send message as contains content which may not be suitable for all audiences");
            disabledDueToBadwords = true;
            return;
        }                                                                                                                                             

        if (disabledDueToBadwords)
        {
            ((GuiTextFieldLockable)inputField).setDisabled("");
            disabledDueToBadwords = false;
            inputField.setEnabled(true);
        }
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1 && sleep)
        {
            wakeFromSleep();
            super.keyTyped(typedChar, keyCode);
            return;
        }

        if ((Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) && ((GuiNewChatOurs) mc.ingameGUI.getChatGUI()).isBase()) {
            inputField.setEnabled(true);
            super.keyTyped(typedChar, keyCode);
            return;
        }

        boolean ourEnabled = ((GuiTextFieldLockable)inputField).getOurEnabled();

        if (!ourEnabled)
        {
            if ((keyCode == 28 || keyCode == 156) && ((Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) && !((GuiNewChatOurs) mc.ingameGUI.getChatGUI()).isBase()))
                return;
            inputField.setEnabled(true);
        }

        super.keyTyped(typedChar, keyCode);

        if (!ourEnabled)
        {
            inputField.setEnabled(false);
        }
        if ((Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) && !((GuiNewChatOurs) mc.ingameGUI.getChatGUI()).isBase()) processBadwords();

    }
    
    public GuiChatOurs(String presetString, boolean sleep)
    {
        super(presetString);
        this.presetString = presetString;
        this.sleep = sleep;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        TimestampComponentString.setFakeActive(true);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        TimestampComponentString.setFakeActive(false);
        if (menuDropdownButton.wasJustClosed && !menuDropdownButton.dropdownOpen)
        {
            menuDropdownButton.xPosition = menuDropdownButton.yPosition = -10000;
            menuDropdownButton.wasJustClosed = false;
        }
    }

    @Override
    public void sendMessage(String msg, boolean addToChat)
    {
        if (!(Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) || ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).isBase())
        {
            super.sendMessage(msg, addToChat);
            return;
        }
        if (msg.startsWith("/"))
        {
            super.sendMessage(msg, addToChat);
            ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).setBase(true);
            return;
        }
        else
        {
            msg = net.minecraftforge.event.ForgeEventFactory.onClientSendMessage(msg);
            if (msg.isEmpty()) return;
            if (addToChat)
            {
                this.mc.ingameGUI.getChatGUI().addToSentMessages(msg);
            }
            if (net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(mc.player, msg) != 0)
            {
                ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).setBase(true);
                return;
            }
            if (ChatHandler.isOnline()) {
                String text = GuiMTChat.getStringForSending(msg);
                //ChatHandler.sendMessage(ChatHandler.CHANNEL, text);
                String currentTarget = ChatHandler.CHANNEL;
                switch (switchButton.activeButton)
                {
                    case 2:
                        if (ChatHandler.hasGroup) {
                        currentTarget = ChatHandler.currentGroup;
                        }
                    break;

                }
                ChatHandler.sendMessage(currentTarget, text);
            }
        }
    }

    private Field tabCompleterField = null;
    public void replaceTabCompleter()
    {
        if (tabCompleterField == null) {
//            tabCompleterField = ReflectionHelper.findField(ChatScreen.class, "tabCompleter", "field_184096_i", "");
            tabCompleterField.setAccessible(true);
        }

        try {
            tabCompleterField.set(this, new OurChatTabCompleter(inputField));
        } catch (IllegalAccessException ignored) {}
    }

    @Override
    public void init()
    {
        if (!presetString.isEmpty())
        {
            if (Minecraft.getInstance().ingameGUI.getChatGUI() instanceof GuiNewChatOurs)
            {
                GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI();
                ourChat.setBase(true);
            }
        }
        super.init();
        TextFieldWidget oldInputField = this.inputField;
        this.inputField = new GuiTextFieldLockable(mc.fontRenderer, 4, this.height - 12, this.width - 4, 12, "");
        this.inputField.setMaxStringLength(256);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused2(true);
        this.inputField.setText(oldInputField.getText());
        this.inputField.setCanLoseFocus(false);
        replaceTabCompleter();
        List<String> strings = new ArrayList<>();

        strings.add(I18n.format("minetogether.chat.button.mute"));
        strings.add(I18n.format("minetogether.chat.button.addfriend"));
        
        int x = MathHelper.ceil(((float) mc.ingameGUI.getChatGUI().getChatWidth())) + 16 + 2;
        String defaultStr = "Default";
        defaultStr = I18n.format("minetogether.ingame.chat.local");
        try {
            if (mc.getCurrentServerData().isOnLAN() || (!mc.getCurrentServerData().serverIP.equals("127.0.0.1"))) {
                defaultStr = I18n.format("minetogether.ingame.chat.server");
            }
        } catch(NullPointerException err){}//Who actually cares? If getCurrentServerData() is a NPE then we've got our answer anyway.
        if(ChatHandler.hasGroup) {
        //if(true) {
            this.buttons.add(switchButton = new GuiButtonPair(808, x, height - 215, 234, 16, !CreeperHost.instance.gdpr.hasAcceptedGDPR() || ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).isBase() ? 0 : ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).chatTarget.equals(ChatHandler.CHANNEL) ? 1 : 2, false, false, true, defaultStr, I18n.format("minetogether.ingame.chat.global"), I18n.format("minetogether.ingame.chat.group")));
        } else {
            this.buttons.add(switchButton = new GuiButtonPair(808, x, height - 156, 156, 16, !CreeperHost.instance.gdpr.hasAcceptedGDPR() || ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).isBase() ? 0 : 1, false, false, true, defaultStr, I18n.format("minetogether.ingame.chat.global")));
        }
        this.buttons.add(menuDropdownButton = new DropdownButton<>(-1337, -1000, -1000, 100, 20, "Menu", new GuiMTChat.Menu(strings), true));
        menuDropdownButton.flipped = true;
        if (sleep)
        {
            this.buttons.add(new Button(1, this.width / 2 - 100, this.height - 40, I18n.format("multiplayer.stopSleeping")));
        }
    }

    @Override
    protected void actionPerformed(Button button) throws IOException
    {
        if (button == menuDropdownButton)
        {
            if (menuDropdownButton.getSelected().option.equals(I18n.format("minetogether.chat.button.mute")))
            {
                CreeperHost.instance.muteUser(activeDropdown);
                GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI();
                ourChat.rebuildChat(ourChat.chatTarget);
                ((GuiNewChatOurs) Minecraft.getInstance().ingameGUI.getChatGUI()).setChatLine(new StringTextComponent(I18n.format("minetogether.chat.muted")), 0, Minecraft.getInstance().ingameGUI.getUpdateCounter(), false);
            }
            else if (menuDropdownButton.getSelected().option.equals(I18n.format("minetogether.chat.button.addfriend")))
            {
                mc.displayGuiScreen(new GuiChatFriend(this, mc.getSession().getUsername(), activeDropdown, Callbacks.getFriendCode(), "", false));
            }
            return;
        }
        else if (button == switchButton)
        {
            if (CreeperHost.instance.gdpr.hasAcceptedGDPR())
            {
                GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
                ourChat.setBase(switchButton.activeButton == 0);
                if (!ourChat.isBase()) {
                    ourChat.rebuildChat(switchButton.activeButton == 1 ? ChatHandler.CHANNEL : ChatHandler.currentGroup);//ChatHandler.privateChatList.getChannelname());
                    processBadwords();
                }
                switchButton.displayString = ourChat.isBase() ? "MineTogether Chat" : "Minecraft Chat";
            }
            else {
                try {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiGDPR(null, () ->
                    {
                        GuiNewChatOurs ourChat = (GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI();
                        if(switchButton.activeButton == 1) {
                            ourChat.setBase(false);
                            ourChat.rebuildChat(ChatHandler.CHANNEL);
                        }
                        return new GuiChatOurs(presetString, sleep);
                    }));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        else if (sleep && button.id == 1)
        {
            wakeFromSleep();
            return;
        }
        super.actionPerformed(button);
    }
    
    @Override
    public void updateScreen()
    {
        if (sleep && !mc.player.isPlayerSleeping())
        {
            mc.displayGuiScreen(null);
        }
        super.updateScreen();
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        for (int i = 0; i < this.buttonList.size(); ++i)
        {
            this.buttonList.get(i).func_191745_a(this.mc, mouseX, mouseY, partialTicks);
        }
        
        for (int j = 0; j < this.labelList.size(); ++j)
        {
            this.labelList.get(j).drawLabel(this.mc, mouseX, mouseY);
        }

        drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
        this.inputField.drawTextBox();

        ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if (!(this.mc.ingameGUI.getChatGUI() instanceof GuiNewChatOurs))
            return;

        GuiNewChatOurs chatGui = (GuiNewChatOurs) mc.ingameGUI.getChatGUI();
        if ((!chatGui.isBase()) && (!chatGui.chatTarget.toLowerCase().equals(ChatHandler.CHANNEL.toLowerCase())) && (!chatGui.chatTarget.toLowerCase().contains(ChatHandler.CHANNEL.toLowerCase())) && (chatGui.chatTarget.length() > 0)&&(!chatGui.chatTarget.toLowerCase().equals("#minetogether")))
        {
            String str = chatGui.closeComponent.getFormattedText();
            int x = mc.ingameGUI.getChatGUI().getChatWidth() - 2;
            int y = height - 40 - (mc.fontRendererObj.FONT_HEIGHT * Math.max(Math.min(chatGui.drawnChatLines.size(), chatGui.getLineCount()), 20));
            mc.fontRendererObj.drawString(str, x, y, 0xFFFFFF);
        }

        //TimestampComponentString.clearActive();

        if (!((GuiTextFieldLockable)inputField).getOurEnabled() && ((GuiTextFieldLockable)inputField).isHovered(mouseX, mouseY))
        {
            drawHoveringText(Arrays.asList(((GuiTextFieldLockable)inputField).getDisabledMessage()), mouseX, mouseY);
        }

        if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null)
        {
            this.handleComponentHover(itextcomponent, mouseX, mouseY);
            if (!TimestampComponentString.getChanged())
                TimestampComponentString.clearActive();
        } else {
            TimestampComponentString.clearActive();
        }

    }
    
    @Deprecated
    public void drawLogo()
    {
        ResourceLocation resourceLocationCreeperLogo = new ResourceLocation("creeperhost", "textures/creeperhost25.png");
        ResourceLocation resourceLocationMinetogetherLogo = new ResourceLocation("creeperhost", "textures/minetogether25.png");
        
        GL11.glPushMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocationCreeperLogo);
        GL11.glEnable(GL11.GL_BLEND);
        Gui.drawModalRectWithCustomSizedTexture(-8, this.height - 80, 0.0F, 0.0F, 40, 40, 40F, 40);
        
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocationMinetogetherLogo);
        Gui.drawModalRectWithCustomSizedTexture(this.width / 2 - 160, this.height - 155, 0.0F, 0.0F, 160, 120, 160F, 120F);
        
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
    
    private void wakeFromSleep()
    {
        NetHandlerPlayClient nethandlerplayclient = this.mc.player.connection;
        nethandlerplayclient.sendPacket(new CPacketEntityAction(this.mc.player, CPacketEntityAction.Action.STOP_SLEEPING));
    }
    
    @Override
    public boolean handleComponentClick(ITextComponent component)
    {
        if (!(Minecraft.getMinecraft().ingameGUI.getChatGUI() instanceof GuiNewChatOurs) || ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).isBase())
        {
            return super.handleComponentClick(component);
        }

        if(component == ((GuiNewChatOurs) Minecraft.getMinecraft().ingameGUI.getChatGUI()).closeComponent)
        {
            CreeperHost.instance.closeGroupChat();
            return true;
        }
        ClickEvent event = component.getStyle().getClickEvent();
        if (event == null)
            return false;
        if (event.getAction() == ClickEvent.Action.SUGGEST_COMMAND)
        {
            String eventValue = event.getValue();
            if (eventValue.contains(":"))
            {
                String[] split = eventValue.split(":");
                if (split.length < 3)
                    return false;
                
                String chatInternalName = split[1];
                
                String friendCode = split[2];
                
                StringBuilder builder = new StringBuilder();
                
                for (int i = 3; i < split.length; i++)
                    builder.append(split[i]).append(" ");
                
                String friendName = builder.toString().trim();
                
                Minecraft.getMinecraft().displayGuiScreen(new GuiChatFriend(this, mc.getSession().getUsername(), chatInternalName, friendCode, friendName, true));
                
                return true;
            }
            int mouseX = Mouse.getX() * width / mc.displayWidth;
            menuDropdownButton.xPosition = mouseX;
            menuDropdownButton.yPosition = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            menuDropdownButton.dropdownOpen = true;
            activeDropdown = event.getValue();
            return true;
        }
        return super.handleComponentClick(component);
    }

    public static class OurChatTabCompleter extends ChatTabCompleter
    {
        private String[] ourCompletions = new String[0];
        private boolean replace = false;

        @Override
        public void setCompletions(String... newCompl) {
            String[] ret;
            if (!replace)
                ret = ArrayUtils.addAll(newCompl, ourCompletions);
            else
                ret = ourCompletions;
            super.setCompletions(ret);
        }

        @Override
        public void complete() {
            GuiNewChatOurs.tabCompletion = true;
            if (!didComplete)
                prepareCompletions();
            super.complete();
            GuiNewChatOurs.tabCompletion = false;
        }

        private void prepareCompletions() {
            String text = textField.getText();
            String[] words = text.split(" ");
            int length = words.length;
            String lastWord = length == 0 ? "" : words[words.length - 1];
            ourCompletions = new String[0];

            if (text.startsWith("/")) {
                replace = false;
            } else {
                if (!isBase()) {
                    ourCompletions = ChatHandler.getOnlineUsers().stream().filter(name -> ChatHandler.anonUsers.containsKey(name) || ChatHandler.friends.containsKey(name)).map(s -> CreeperHost.instance.getNameForUser(s)).filter(nick -> nick.toLowerCase().startsWith(lastWord.toLowerCase())).toArray(String[]::new);
                    replace = true;
                }
            }
        }

        public OurChatTabCompleter(GuiTextField p_i46749_1_) {
            super(p_i46749_1_);
        }
    }
}