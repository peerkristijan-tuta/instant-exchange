import java.io.*;
import java.util.Scanner;
import java.net.*;
import java.util.*;

class PortNumbers implements Serializable {
    Integer port1, port2 = null;
    boolean port1_taken, port2_taken = false;
}

class InstantExchange {
    static int port;
    static int port_code;
    static PortNumbers PN;
    static Scanner scanner = new Scanner(System.in);
    static DatagramSocket DS;
    static byte[] buffer = new byte[1000];

    static Integer converter(String input) {
        return Integer.valueOf(input);
    }

    static byte[] converter(char[] characters) {
        byte[] bytes_var = new byte[characters.length];
        for (int x = 0; x < characters.length; x++)
            bytes_var[x] = (byte) characters[x];
        return bytes_var;
    }

    static String converter(byte[] characters) {
        String input = "";
        for (int x = 0; x < characters.length; x++)
            input += (char) characters[x];
        return input;
    }

    static void serialize(PortNumbers ob) {
        try (ObjectOutputStream OOS = 
             new ObjectOutputStream(new FileOutputStream("Port Numbers")))
        {
            OOS.writeObject(ob);
        } catch (IOException exc) {
            System.out.println(exc + "at serialize method");
        }
    }

    static PortNumbers deserialize() throws Exception {
        try (ObjectInputStream OIS = 
             new ObjectInputStream(new FileInputStream("Port Numbers")))
        {
            PortNumbers PN = (PortNumbers) OIS.readObject();
            serialize(PN);
            return PN;
        } catch (FileNotFoundException exc) {
            throw new FileNotFoundException();
        } catch (IOException exc) {
            System.out.print(exc + " at deserialize method");
        }

        return new PortNumbers();
    }

    static int validityCheck(int port_number) {
        if (PN.port1_taken == false && PN.port2_taken == false)
            return port_number;
        else {
            if (PN.port1_taken) {
                if (PN.port1 == port_number) {
                    System.out.print("Port number taken. Provide another: ");
                    return validityCheck(converter(validityCheck(scanner.nextLine())));
                } else {
                    return port_number;
                }
            } else {
                if (PN.port2 == port_number) {
                    System.out.print("Port number taken. Provide another: ");
                    return validityCheck(converter(validityCheck(scanner.nextLine())));
                } else
                    return port_number;
            }
        }
    }

    static String validityCheck(String port_number) {
        Scanner scanner = new Scanner(System.in);
        for (int x = 0; x < port_number.length(); x++) {
            switch (port_number.charAt(x)) {
                case '1', '2', '3', '4', '5', '6', '7', '8', '9', '0':
                    break;
                case 'x', '-', '+', '/', '(', ')':
                    System.out.print("No operator allowed in port number input. Provide another port number: ");
                    return validityCheck(scanner.nextLine());
                default:
                    System.out.print("No alphabets or miscellaneaous characters allowed. Provide another port number: ");
                    return validityCheck(scanner.nextLine());
            }
        }
        return port_number;        
    }

    synchronized static void submit(int port_number)
    throws Exception {
        try {
            PN = deserialize();
            if (PN.port1_taken) {
                PN.port2 = validityCheck(port_number);
                port_code = PN.port2;
                port = 2;
                PN.port2_taken = true;
                serialize(PN);
            } else {
                PN.port1 = validityCheck(port_number);
                port_code = PN.port1;
                port = 1;
                PN.port1_taken = true;
                serialize(PN);
            }
        } catch (FileNotFoundException FNFE) {
            port = 1;
            PN = new PortNumbers();
            PN.port1 = port_number;
            PN.port1_taken = true;
            serialize(PN);
        }
    }

    static void initialization() throws Exception {
        try {
            PN = deserialize();      
            System.out.print("Provide port number: ");
            submit(converter(validityCheck(scanner.nextLine())));
        } catch (FileNotFoundException FNFE) {
            System.out.print("Provide port number: ");
            submit(converter(validityCheck(scanner.nextLine())));
        } catch (NoSuchElementException NSEE) {
        }
    }

    static void network() throws Exception {
        try {
          Runtime.getRuntime().addShutdownHook(new Thread(() -> { 
            if (port == 1) {
                try {
                    PN = deserialize();
                    PN.port1 = null;
                    PN.port1_taken = false;
                    serialize(PN);
                } catch (Exception e) {
                    System.out.println(e + " at network()");
                }
            } else {
                try {
                    PN = deserialize();
                    PN.port2 = null;
                    PN.port2_taken = false;
                    serialize(PN);
                    serialize(PN);
                } catch (Exception e) {
                    System.out.println(e + " at network()");
                }
            }  
          }));
        } catch (NoSuchElementException NSEE) {}

        PN = deserialize();

        if (port == 1) {
            DS = new DatagramSocket(PN.port1);
        } else {
            DS = new DatagramSocket(PN.port2);
        }
        
        class Send extends Thread {
            public void run() {
                while (true) {
                    try {
                        String input = scanner.nextLine();
                        PN = deserialize();
                        switch (input) {
                        case "exit":
                            int current_port;
                            if (port == 1)
                                current_port = PN.port1;
                            else
                                current_port = PN.port2;

                            PN = deserialize();
                        
                            if (port == 1) {
                                PN.port1 = null;
                                PN.port1_taken = false;
                            } else {
                                PN.port2 = null;
                                PN.port2_taken = false;
                            }
                        
                            serialize(PN);

                            DS.send(
                                new DatagramPacket(converter(input.toCharArray()),
                                (input.length()),
                                InetAddress.getLocalHost(),
                                current_port)
                            );

                            return;
                        default:
                            PN = deserialize();
                            if (port == 1) {
                                if (PN.port2 == null) {
                                    System.out.println("No correspondent.");
                                    break;
                                } else {
                                    DS.send(
                                    new DatagramPacket(converter(input.toCharArray()),
                                    (input.length()),
                                    InetAddress.getLocalHost(),
                                    PN.port2)
                                    );
                                }
                            } else {
                                if (PN.port1 == null) {
                                    System.out.println("No correspondent.");
                                    break;
                                } else {
                                    DS.send(
                                    new DatagramPacket(converter(input.toCharArray()),
                                    (input.length()),
                                    InetAddress.getLocalHost(),
                                    PN.port1)
                                    );
                                }
                            }
                        }
                    } catch (NoSuchElementException NSEE) {
                    } catch (Exception exc) {
                        System.out.println(exc + " at Send class");
                    }
                }
            }
        }

        class Receive extends Thread {
            public void run() {
                while (true) {
                    try {
                        DatagramPacket DP = new DatagramPacket(buffer, 1000);
                        DS.receive(DP);

                        if ((new String(DP.getData(), 0, DP.getLength())).equals("exit"))
                            return;
                        else {
                            try {
                                PN = deserialize();
                            } catch (Exception exc) {}
                            System.out.print("[FROM PORT ");
                            if (port == 1)
                                System.out.print(PN.port2 + "]: ");
                            else 
                                System.out.print(PN.port1 + "]: ");
                            System.out.println(new String(DP.getData(), 0, DP.getLength()));
                        }
                    } catch (Exception exc) {
                        System.out.println(exc + " at Receive class");
                    }
                }
            }
        }

        Send send = new Send();
        Receive receive = new Receive();

        send.start();
        receive.start();
    }

    public static void main(String[] args) throws Exception {
        initialization();
        network();
    }
}