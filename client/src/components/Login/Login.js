import PersonIcon from '@mui/icons-material/Person';
import KeyIcon from '@mui/icons-material/Key';
import { Input, InputAdornment } from '@mui/material';
import { useState, useEffect, useRef } from 'react';
import { RESPONSE_QUEUE_SERVER_PORT, WEBSOCKET_URL } from '../../components/WebSocket/WebSocket';
import './index.css';

import { CircularProgress } from '@mui/material';


function createWebSocket(port) {
    return new WebSocket(port);
}

function Login(props) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');


    const handleLogin = () => {
        props.setIsLoading(true);
        const enteredUsername = username.trim();
        const enteredPass = password.trim()
        console.log("Entered username for login: ", enteredUsername);
        console.log("Entered password for login ", enteredPass);

        const newWebSocket = createWebSocket(WEBSOCKET_URL);
        const payload = { requestType: "LOGIN", userName: enteredUsername, password: enteredPass };

        newWebSocket.addEventListener('open', () => {
            console.log('WebSocket connection established!');

            console.log(newWebSocket);


            if (newWebSocket && newWebSocket.readyState === WebSocket.OPEN) {
                newWebSocket.send(JSON.stringify(payload));
            }


            else {
                console.log("WEB SOCKET CONNECTION IS NOT OPEN!")
            }

            newWebSocket.close();

        });
    };

    const handleRegister = () => {
        props.setIsLoading(true);
        const enteredUsername = username.trim();
        const enteredPass = password.trim()
        console.log("Entered username for login: ", enteredUsername);
        console.log("Entered password for login ", enteredPass);

        const newWebSocket = createWebSocket(WEBSOCKET_URL);
        const payload = { requestType: "REGISTER", userName: enteredUsername, password: enteredPass };

        newWebSocket.addEventListener('open', () => {
            console.log('WebSocket connection established!');

            console.log(newWebSocket);


            if (newWebSocket && newWebSocket.readyState === WebSocket.OPEN) {
                newWebSocket.send(JSON.stringify(payload));
            }


            else {
                console.log("WEB SOCKET CONNECTION IS NOT OPEN!")
            }

            newWebSocket.close();

        });
    };

    return (
        <div className="login">
            <h1>Login or Register</h1>
            <h4 className="subheading">to continue to Distributed File System</h4>
            {props.loginStatus === 'failed' && <p className="error-message">Login or Registration failed. Please try again.</p>}
            <form>
                <div className="form-group">
                    <Input
                        type="text"
                        className="input-field"
                        placeholder="Username"
                        id="username"
                        value={username}
                        onChange={(e) => {
                            setUsername(e.target.value);
                            props.updateUser(e.target.value);
                        }}
                        startAdornment={
                            <InputAdornment position="start">
                                <PersonIcon />
                            </InputAdornment>
                        }
                    />
                </div>

                <div className="form-group">
                    <Input
                        type="password"
                        className="input-field"
                        placeholder="Password"
                        id="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        startAdornment={
                            <InputAdornment position="start">
                                <KeyIcon />
                            </InputAdornment>
                        }
                    />
                </div>

                <div className="button-group">
                    <button type="button" className="login-button buttons-for-login" onClick={handleLogin}>
                        Login
                    </button>
                    <button type="button" className="register-button buttons-for-login" onClick={handleRegister}>
                        Register
                    </button>
                </div>
            </form>
        </div>
    );
}

export default Login