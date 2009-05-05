package it.infocamere.freesigner.gui;

public abstract class AbstractTask {

    abstract void setCanceled(String aMessage);
    
    abstract void setMessage(String aMessage);
    
    abstract void setStatus(int code, String aMessage);

}
