package com.hugo99j.chaosparty.ui.element;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.hugo99j.chaosparty.ui.renderable.RenderState;
import com.hugo99j.chaosparty.ui.renderable.UiElement;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.Logger;
import com.hugo99j.chaosparty.util.RenderUtil;

public abstract class TextInput extends UiElement {
    private String prefix;
    private String suffix;
    private int maxLength;
    private boolean allowsNewLines;
    private String value;
    private final String backgroundTexture;
    private int cursorPos;
    private float blinkTimer = 0;
    private boolean wasSelected = false;
    private InputProcessor oldProcessor;

    public TextInput(String id, int maxLength, boolean allowsNewLines, String prefix, String startingValue, String suffix, String backgroundTexture) {
        super(id);
        this.prefix = prefix;
        this.maxLength = maxLength;
        this.allowsNewLines = allowsNewLines;
        this.backgroundTexture = backgroundTexture;
        this.value = startingValue;
        this.suffix = suffix;
        this.usesMouse = true;
        this.cursorPos = this.value.length();
    }

    @Override
    public void render(RenderState state) {
        super.render(state);
        blinkTimer += state.time();

        //safety checks
        if(this.value.length() > this.maxLength) this.value = this.value.substring(0, this.maxLength);
        if(this.cursorPos > this.value.length()) cursorPos = this.value.length();
        if(this.cursorPos < 0) this.cursorPos = 0;

        String cleanValue = this.value.replace("<", "\\<");
        //RenderUtil.TextData realTextInfo = RenderUtil.renderText(this.prefix+cleanValue+this.suffix, this.getStyle());
        if(this.isSelected()) {
            if((blinkTimer % 1) < 0.5f) {
                String cursorText = "<colour:clear>" + (this.prefix + cleanValue.substring(0, this.cursorPos)).replace("<colour:", "<hidden:") + "<colour:white>|";
                //RenderUtil.TextData fakeTextInfo = RenderUtil.getInfoAbout(cursorText, this.getStyle());

                //int diff = (int) (realTextInfo.width() - fakeTextInfo.width());
                //if (diff > 0) cursorText += GameData.getSpace(diff);

                //RenderUtil.renderText(cursorText, this.getStyle());
            }

            if(!this.wasSelected) {
                oldProcessor = Gdx.input.getInputProcessor();
                Gdx.input.setInputProcessor(new InputProcessor() {
                    @Override
                    public boolean keyDown(int keycode) {
                        blinkTimer = 0;
                        if(keycode == Input.Keys.LEFT) {
                            cursorPos--;
                        } else if(keycode == Input.Keys.RIGHT) {
                            cursorPos++;
                        }
                        if(keycode == Input.Keys.V && (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
                            String s = Gdx.app.getClipboard().getContents();
                            for (int i = 0; i < s.length(); i++) {
                                keyTyped(s.charAt(i));
                            }
                        }
                        return true;
                    }

                    @Override
                    public boolean keyUp(int keycode) {
                        return true;
                    }

                    @Override
                    public boolean keyTyped(char character) {
                        blinkTimer = 0;
                        //noinspection StatementWithEmptyBody
                        if(character == '\n' && !allowsNewLines) { //ENTER key

                        } else if(character == '\b') { //BACKSPACE key
                            if(!value.isEmpty() && cursorPos >= 1) {
                                value = new StringBuilder(value).deleteCharAt(cursorPos-1).toString();
                                cursorPos--;
                            }
                        } else if(character == '\u007F') { //DELETE key
                            if(!value.isEmpty() && cursorPos < value.length()) {
                                value = new StringBuilder(value).deleteCharAt(cursorPos).toString();
                            }
                        } else if(character == '\t') { //TAB key
                            value += "    ";
                            cursorPos+=4;
                        } else if(Character.isSpaceChar(character)) { //any type of space
                            value += " ";
                            cursorPos++;
                        } else {
                            if(!Character.isAlphabetic(character) && !Character.isDigit(character)) {
                                Logger.error("Unknown character: "+character);
                            }
                            value = new StringBuilder(value).insert(cursorPos, character).toString();
                            cursorPos++;
                        }
                        return true;
                    }

                    @Override
                    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                        return true;
                    }

                    @Override
                    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                        return true;
                    }

                    @Override
                    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
                        return true;
                    }

                    @Override
                    public boolean touchDragged(int screenX, int screenY, int pointer) {
                        return true;
                    }

                    @Override
                    public boolean mouseMoved(int screenX, int screenY) {
                        return true;
                    }

                    @Override
                    public boolean scrolled(float amountX, float amountY) {
                        return true;
                    }
                });
            }
        } else {
            this.blinkTimer = 0;
            this.cursorPos = this.value.length();
            if(this.wasSelected) Gdx.input.setInputProcessor(this.oldProcessor);
            this.oldProcessor = null;
        }


        this.wasSelected = this.isSelected();
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setCursorPos(int cursorPos) {
        this.cursorPos = cursorPos;
    }

    public void setAllowsNewLines(boolean allowsNewLines) {
        this.allowsNewLines = allowsNewLines;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if(!this.value.equals(value)) {
            this.cursorPos = value.length();
        }
        this.value = value;
    }

    @Override
    public void dispose() {
        super.dispose();
        if(this.wasSelected) Gdx.input.setInputProcessor(null);
    }
}
