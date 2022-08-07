import React, { useState } from 'react';
import './App.css';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Grid from '@material-ui/core/Grid';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import { styles } from "./Props";
import { withStyles } from '@material-ui/core/styles';
import AnalysedTweetsChart from './AnalysedTweetsChart'

let sse;

const App = (props) => {
  const [hashtags, setHashtags] = useState('');

  const [analyzedTweets, setAnalyzedTweets] = useState([]);
  const [polarityResults, setPolarityResults] = useState([]);
  const [subjectivityResults, setSubjectivityResults] = useState([]);

  const { classes } = props;

  const sendHashtags = (event) => {
    event.preventDefault()

    if (sse) { 
      sse.close()

      setAnalyzedTweets([]);
      setPolarityResults([]);
      setSubjectivityResults([]);
    }

    if (hashtags) {
      const parsedHashtags = hashtags.replaceAll('#', '').replaceAll(' ', '')
      console.log(parsedHashtags)

      sse = new EventSource('http://localhost:8080/sentiment?hashtags=' + parsedHashtags);
    
      sse.onmessage = e => {
        const analyzedTweet = JSON.parse(e.data)
        console.log(analyzedTweet)
  
        setAnalyzedTweets(res  => [...res, analyzedTweet]);
        setPolarityResults(res  => [...res, analyzedTweet.polarity]);
        setSubjectivityResults(res  => [...res, analyzedTweet.subjectivity]);
      }
      
      sse.onerror = e => {
        console.log(e)
        sse.close();
      }

      setTimeout(() => { sse.close(); }, 600000);
    }
  }

  const polaritySum = polarityResults.reduce((a, b) => a + b, 0);
  const polarityAvg = (polaritySum / polarityResults.length) || 0;

  const subjectivitySum = subjectivityResults.reduce((a, b) => a + b, 0);
  const subjectivityAvg = (subjectivitySum / subjectivityResults.length) || 0;

  return (
    <div className="App">
      <h1>Twitter Sentiment Analysis</h1>
      <Grid container spacing={2}>
        <Grid item xs={12}>
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
        </Grid>
        <Grid item xs={6}>
          <AnalysedTweetsChart
            label={"Polarity"}
            color={'rgb(194, 16, 57)'}
            data={polarityResults}
            dataMin={-100}
            dataMax={100}
          />
        </Grid>
        <Grid item xs={6}>
          <AnalysedTweetsChart
            label={"Subjectivity"}
            color={'rgb(29, 98, 219)'}
            data={subjectivityResults}
            dataMin={0}
            dataMax={100}
          />
        </Grid>
        <Grid item xs={6}>
          Average: {polarityAvg.toFixed(2)}
        </Grid>
        <Grid item xs={6}>
          Average: {subjectivityAvg.toFixed(2)}
        </Grid>
        {
          analyzedTweets.length > 0 && <>
              <Grid item xs={2}/>
              <Grid item xs={8}>
                <h2>Analyzed Tweets:</h2>
                <TableContainer component={Paper}>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>#</TableCell>
                        <TableCell>Tweet</TableCell>
                        <TableCell align="right">Polarity</TableCell>
                        <TableCell align="right">Subjectivity</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {analyzedTweets.map((analyzedTweet, i) => (
                        <TableRow key={i}>
                          <TableCell component="th" scope="row">{i}</TableCell>
                          <TableCell>{analyzedTweet.tweet}</TableCell>
                          <TableCell align="right">{analyzedTweet.polarity.toFixed(2)}</TableCell>
                          <TableCell align="right">{analyzedTweet.subjectivity.toFixed(2)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>
              <Grid item xs={2}/>
              </>
          }
      </Grid>
    </div>
  );
}

export default withStyles(styles)(App);
