package dev.shwg.shwgconfig.gui.input.events;

/*? if >= 1.21.9 { */
import net.minecraft.client.input.CharacterEvent;
 /*? } */

public class GenericCharacterEvent {

    private final int codepoint;
    private final int modifiers;

    /*? if >= 1.21.9 { */
    public GenericCharacterEvent(CharacterEvent event) {
        this.codepoint = event.codepoint();
        /*? if >= 26 { */
        this.modifiers = 0;
        /*? } else { */
        //this.modifiers = event.modifiers();
        /*? } */
    }
    /*? } else { */
    /*public  GenericCharacterEvent(char character, int modifiers) {
        this.codepoint = character;
        this.modifiers = modifiers;
    }
    *//*? } */

    public int getCodepoint() {
        return codepoint;
    }

    public int getModifier() {
        return modifiers;
    }

    public char getCodepointAsChar() {
        return (char) codepoint;
    }

    public String getCodepointAsString() {
        return Character.toString(codepoint);
    }
}
