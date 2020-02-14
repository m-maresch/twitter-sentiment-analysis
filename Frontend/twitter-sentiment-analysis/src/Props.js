import {createStyles} from "@material-ui/core";

export const styles = (theme) =>
    createStyles({
        container: {
            display: 'flex',
            flexWrap: 'wrap',
        },
        textField: {
            marginLeft: theme.spacing.unit,
            marginRight: theme.spacing.unit,
            width: 200,
        },
        button: {
            margin: theme.spacing.unit,
        },
    });
