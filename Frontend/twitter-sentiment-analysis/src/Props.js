import {createStyles} from "@material-ui/core";

export const styles = (theme) =>
    createStyles({
        container: {
            display: 'flex',
            flexWrap: 'wrap',
        },
        textField: {
            marginLeft: theme.spacing(1),
            marginRight: theme.spacing(1),
            width: 500,
        },
        button: {
            margin: theme.spacing(1),
        },
    })
