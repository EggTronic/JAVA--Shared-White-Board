package Exceptions;

@SuppressWarnings("serial")
public class AbnormalCommunicationException extends Exception {

    public AbnormalCommunicationException () {

    }
        public AbnormalCommunicationException (String message) {
            super(message);
        }
    }