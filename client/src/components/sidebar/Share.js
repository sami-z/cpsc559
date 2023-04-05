import React, { useState } from 'react';
import { WEBSOCKET_URL } from '../WebSocket/WebSocket';
import './styles.css'
import ScreenShareIcon from '@mui/icons-material/ScreenShare';
import Modal from '@mui/material/Modal';
import { makeStyles } from '@mui/styles';

function getModalStyle() {
    return {
        top: `50%`,
        left: `50%`,
        transform: `translate(-50%, -50%)`,
    };
}

const useStyles = makeStyles({
    root: {
        position: 'absolute',
        background: 'white',
        border: '2px solid #000',
        borderRadius: 3,
        color: 'black',
        height: 48,
        padding: '0 30px',
    },
});

function ShareButton({ selectedFiles, userName }) {
    const classes = useStyles();
    const [names, setNames] = useState('');
    const [open, setOpen] = useState(false);
    const [modalStyle] = useState(getModalStyle);
    const [sharing, setUploading] = useState(false);

    const handleNameChange = (event) => {
        setNames(event.target.value);
    };

    const handleClose = () => {
        setOpen(false);
    };


    const handleOpen = () => {
        setOpen(true);
    };


    function createWebSocket() {
        return new WebSocket(WEBSOCKET_URL);
    }

    const handleSharePermission = () => {
        const newWebSocket = createWebSocket();
        // Send a JSON payload through the WebSocket connection to share the permission with the specified user
        const shareWithArr = names.trim().split(",");
        const fileNames = selectedFiles.map(item => item.fileName);
        const payload = { requestType: "SHARE", userName: userName, filesToShare: fileNames, shared: shareWithArr };

        newWebSocket.addEventListener('open', () => {
            console.log('WebSocket connection established!');

            console.log(newWebSocket);

            if (newWebSocket && newWebSocket.readyState === WebSocket.OPEN) {
                newWebSocket.send(JSON.stringify(payload));
                alert('Permission shared successfully!');
            }

            else {
                console.log("WEB SOCKET CONNECTION IS NOT OPEN!")
            }

            newWebSocket.close()
            console.log(newWebSocket);

            // Send the file then close
            // handleFileUpload();
            // handleClose();
        });

        console.log(JSON.stringify(payload))
    };

    return (
        <div className='upload'>
            <div className='upload__container' onClick={handleOpen}>
                <ScreenShareIcon />
                <p className='side-button-container'>Share</p>
            </div>

            <Modal
                open={open}
                onClose={handleClose}
                aria-labelledby="simple-modal-title"
                aria-describedby="simple-modal-description"
            >

                {/* // {classes.paper} */}
                <div style={modalStyle} className={classes.root}>
                    <center>
                        <p>Enter user(s) to share with:</p>
                    </center>
                    {
                        sharing ? (
                            <p>Sharing...</p>
                        ) : (
                            <>
                                <input type="text" placeholder="Enter username(s)" value={names} onChange={handleNameChange} />
                                <button onClick={handleSharePermission}>Share File</button>
                                {/* <button onClick={handleFileUpload}>Upload to DFS</button> */}
                                {/* {fileData && <p>{fileData}</p>} */}
                            </>
                        )
                    }
                </div>
            </Modal>
        </div>
    );
}

export default ShareButton;
