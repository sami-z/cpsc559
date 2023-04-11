import React, { useState } from 'react'
import DeleteIcon from '@mui/icons-material/Delete';
import './styles.css'
import { REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT, createWebSocket } from '../WebSocket/WebSocket';



import { makeStyles } from '@mui/styles';
import Modal from '@mui/material/Modal';

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

function Delete(props) {
    const classes = useStyles();


    const [uploadStatus, setUploadStatus] = useState('idle'); // 'idle', 'uploading', 'uploaded'
    const [modalStyle] = useState(getModalStyle);
    const [open, setOpen] = useState(false);
    const [fileData, setFileData] = useState(null);
    const [fileBytes, setFileBytes] = useState(null);
    const [uploading, setUploading] = useState(false);

    const handleDelete = async () => {

        for (const file of props.selectedFiles) {
            if (file.userName !== props.currentUser) {
              alert('You cannot delete files you do not own!');
              return;
            }
          }

        setUploadStatus('uploading');

        createWebSocket(REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT)
        .then((ws) => {
            console.log('WebSocket connection established:', ws);
            const payload = { requestType: "DELETE", currentUser: props.currentUser, writeType: "DELETE", filesToDelete: props.selectedFiles };
            ws.send(JSON.stringify(payload));
            setTimeout(() => {
                setUploadStatus('uploaded');
                setOpen(false);
                setFileData(null);
                setFileBytes(null);
                setUploadStatus('idle');
            }, 2500); // add a 2.5 second delay
            ws.close();

        })
        .catch((error) => {
            console.error(`An error occurred while connecting to a WebSocket: ${error}`);
        });
    };

    const handleOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };
    return (
        <div className='upload'>
            <div className='upload__container' onClick={handleOpen}>
                <DeleteIcon />
                <p className='side-button-container'>Delete</p>
            </div>

            <Modal
                open={open}
                onClose={handleClose}
                aria-labelledby="simple-modal-title"
                aria-describedby="simple-modal-description"
            >
                <div style={modalStyle} className={classes.root}>
                    <center>
                        <p>Delete the selected file(s)?:</p>
                    </center>
                    {
                        uploadStatus === 'uploading' ? (
                            <div>
                                <center>
                                    <p>Deleting...</p>
                                    <div className="loading-spinner"></div>
                                </center>

                            </div>

                        ) : (
                            <>
                                <center>
                                    <button onClick={handleDelete}>Confirm</button>
                                </center>
                            </>
                        )
                    }
                </div>
            </Modal>
        </div>
    )
}

export default Delete
