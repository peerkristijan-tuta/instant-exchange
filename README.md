- Notable elements in code

UDP port numbers are reserved in a file that is named Port Numbers in order to avoid port number conflicts and to allow for the discovery of another instance with a valid port number. In addition to that, ShutdownHook of the Runtime package is used to ensure that abnormal program termination doesn't corrupt the port number file by mandating that the program withdraw its port number reservation at Port Numbers at termination. 
- Implementation Guidelines

Run it. Then specify a UDP port number and start sending and receiving messages if another instance of the program is running and has specified a UDP port number.
