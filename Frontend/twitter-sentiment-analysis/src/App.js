import React, { useState, useEffect } from 'react';
import './App.css';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import Grid from '@material-ui/core/Grid';
import { styles } from "./Props";
import { withStyles } from '@material-ui/core/styles';
import { Client } from '@stomp/stompjs';
import AnalysedTweetsChart from './AnalysedTweetsChart'

// Set up for testing whether the basic dataflow is working correctly
// Integration tests follow after the MVP is done
const App = (props) => {
  const [hashtags, setHashtags] = useState('');

  const [results, setResults] = useState([]);

  const { classes } = props;

  const client = new Client({
    brokerURL: "ws://localhost:8080/socket",
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000
  });

  const [ws] = useState(client)

  useEffect(() => {
    ws.onConnect = (frame) => ws.subscribe("/analysed-sentiment", (message) => setResults(res  => [...res, message.body]))
    
    ws.activate()

    return () => {
      ws.deactivate()
    }
  }, [ws]);   

  const sendHashtags = (event) => {
    fetch('http://localhost:8080/api/sentiment?hashtags=' + hashtags, 
    { 
      method: 'POST', 
      headers: {
        'Content-Type': 'application/json'
      }
    })
    .then((response) => {
      ws.publish({destination: '/api/send/hashtags', body: hashtags.split(',')
        .join('')
        .split(' ')
        .join('')})
    })

    event.preventDefault()
  }

  return (
    <div className="App">
      <Grid container justify = "center">
        <form className={classes.container} noValidate onSubmit={sendHashtags}>
            <TextField
                id="hashtags"
                className={classes.textField}
                label="Enter Twitter #'s (e.g. #something1, #something2, #something3,...)"
                value={hashtags}
                onChange={(e) => setHashtags(e.target.value)}
                margin="normal"
            />
            <Button className={classes.button} variant="contained" color="primary" type="submit">
                Analyse
            </Button>
        </form>  
      </Grid>    
      <List component="nav" aria-label="main mailbox folders">      
      {        
        results.map((res, i) => {
          return (
            <ListItem button key={i}>
              <ListItemText primary={res} />
            </ListItem>
          )
        })
      }
      </List>
      <AnalysedTweetsChart
      data={[
        {
          "name": "Something 1",
          "value": 20
        },
        {
          "name": "Something 2",
          "value": 30
        }
      ]}
      title={'Test'}
      />
    </div>
  );
}

export default withStyles(styles)(App);
