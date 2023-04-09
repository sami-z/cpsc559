import React, { useState } from 'react';
import { WEBSOCKET_URL } from '../WebSocket/WebSocket';
import './styles.css'
import StopScreenShareIcon from '@mui/icons-material/StopScreenShare';
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

function UnshareButton({ selectedFiles, currentUser }) {
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
        const unshareWithArr = names.trim().split(",");
        const fileNames = selectedFiles.map(item => item.fileName);
        const payload = { requestType: "UNSHARE", currentUser: currentUser, filesToUnshare: fileNames, unshared: unshareWithArr  };

        newWebSocket.addEventListener('open', () => {
            console.log('WebSocket connection established!');

            console.log(newWebSocket);

            if (newWebSocket && newWebSocket.readyState === WebSocket.OPEN) {
                newWebSocket.send(JSON.stringify(payload));
                alert('Permission unshared successfully!');
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
                <StopScreenShareIcon />
                <p className='side-button-container'>Unshare</p>
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
                        <p>Enter user(s) to un-share with:</p>
                    </center>
                    {
                        sharing ? (
                            <p>Sharing...</p>
                        ) : (
                            <>
                                <input type="text" placeholder="Enter username(s)" value={names} onChange={handleNameChange} />
                                <button onClick={handleSharePermission}>Unshare File</button>
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

export default UnshareButton;
