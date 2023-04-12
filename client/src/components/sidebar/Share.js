import React, { useState } from 'react';
import { REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT, createWebSocket } from '../WebSocket/WebSocket';
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

function ShareButton({ selectedFiles, currentUser }) {
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

    const handleSharePermission = () => {

        for (const file of selectedFiles) {
            if (file.userName !== currentUser) {
              alert('You cannot share files you do not own!');
              return;
            }
        }

        createWebSocket(REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT)
        .then((ws) => {
            console.log('WebSocket connection established:', ws);
            const shareWithArr = names.trim().split(",");
            const fileNames = selectedFiles.map(item => item.fileName);

            const payload = { requestType: "SHARE", currentUser: currentUser, filesToShare: fileNames, shared: shareWithArr };
            ws.send(JSON.stringify(payload));
            alert('Permission shared successfully!');
            ws.close();
            handleBlur();
        })
        .catch((error) => {
            console.error(`An error occurred while connecting to a WebSocket: ${error}`);
            handleBlur();
        });
    };

    function handleBlur() {
        setNames('') 
    }

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
                                <button onClick={handleSharePermission} onBlur={handleBlur}>Share File</button>
                            </>
                        )
                    }
                </div>
            </Modal>
        </div>
    );
}

export default ShareButton;
