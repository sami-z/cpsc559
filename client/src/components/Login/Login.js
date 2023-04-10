import PersonIcon from '@mui/icons-material/Person';
import KeyIcon from '@mui/icons-material/Key';
import { Input, InputAdornment } from '@mui/material';
import { useState, useEffect, useRef } from 'react';
import { REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT, createWebSocket } from '../../components/WebSocket/WebSocket';

import './index.css';

function Login(props) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');


    const handleLogin = () => {
        props.setIsLoading(true);
        const enteredUsername = username.trim();
        const enteredPass = password.trim()
        props.updateUser(enteredUsername);
        console.log("Entered username for login: ", enteredUsername);
        console.log("Entered password for login ", enteredPass);


        createWebSocket(REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT)
        .then((ws) => {
            console.log('WebSocket connection established:', ws);
            const payload = { requestType: "LOGIN", currentUser: enteredUsername, password: enteredPass };
            ws.send(JSON.stringify(payload));
            ws.close();

        })
        .catch((error) => {
            console.error(`An error occurred while connecting to a WebSocket: ${error}`);
        });
    };

    const handleRegister = () => {
        props.setIsLoading(true);
        const enteredUsername = username.trim();
        const enteredPass = password.trim()
        props.updateUser(enteredUsername);
        console.log("Entered username for login: ", enteredUsername);
        console.log("Entered password for login ", enteredPass);

        createWebSocket(REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT)
        .then((ws) => {
            console.log('WebSocket connection established:', ws);
            const payload = { requestType: "REGISTER", currentUser: enteredUsername, password: enteredPass };
            ws.send(JSON.stringify(payload));
            ws.close();

        })
        .catch((error) => {
            console.error(`An error occurred while connecting to a WebSocket: ${error}`);
        });
    };

    return (
        <div className="login">
            <h1>Login or Register</h1>
            <h4 className="subheading">to continue to FileArc</h4>
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