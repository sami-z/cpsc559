import React, {useState} from 'react'
import AddIcon from '@mui/icons-material/Add'
import '../../components/sidebar/Upload.css'

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

function sendText(socket) {
    socket.send("If you are reading this then it's too late - Drake");
}

const Upload = (props) => {
    const { socket } = props;
    const classes = useStyles();

    const [modalStyle] = useState(getModalStyle);
    const [open, setOpen] = useState(false);
    const [fileData, setFileData] = useState(null);
    const [uploading, setUploading] = useState(false);

    const handleOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };

    const handleChange = (e) => {
        if (e.target.files[0]) {
            const file = e.target.files[0];
            const reader = new FileReader();
            reader.onload = (e) => {
                setFileData(e.target.result);
            };
            reader.readAsText(file);
        }
    }

    const handleFileUpload = (event) => {
        setUploading(true);
        const file = event.target.files[0];
        const reader = new FileReader();
        reader.onload = (e) => {
          setFileData(e.target.result);
        };
        reader.readAsText(file);
      };

  return (
    <div className='upload'>
        <div className='upload__container' onClick={handleOpen}>
            <AddIcon/>
            <p>Upload</p>
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
                        <p>Select file(s) to upload:</p>
                    </center>
                    {
                        uploading ? (
                            <p>Uploading...</p>
                        ) : (
                                <>
                                    <input type="file" onChange={handleChange} />
                                    <button onClick={sendText(socket)}>Upload to DFS</button>
                                    {/* <button onClick={handleFileUpload}>Upload to DFS</button> */}
                                    {/* {fileData && <p>{fileData}</p>} */}
                                </>
                            )
                    }
                </div>
            </Modal>
    </div>
  )
}

export default Upload
