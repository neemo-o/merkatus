import PropTypes from "prop-types";
import { Eye, Pencil, Trash2, RotateCcw, Power } from "lucide-react";
import Button from "./Button";

const iconMap = {
  view: Eye,
  edit: Pencil,
  delete: Trash2,
  toggle: Power,
  renew: RotateCcw,
};

import { Link } from "react-router-dom";

export default function IconButton({
  action,
  tooltip,
  to,
  onClick,
  size = "sm",
  variant = "ghost",
  ...props
}) {
  const IconComponent = iconMap[action];

  if (!IconComponent) {
    return null;
  }

  const sharedProps = {
    variant,
    size,
    className: "p-2 h-9 w-9",
    title: tooltip,
    "aria-label": tooltip,
    ...props,
  };

  const content = <IconComponent className="h-4 w-4" />;

  if (to) {
    return (
      <Link to={to} className="block">
        <Button {...sharedProps}>{content}</Button>
      </Link>
    );
  }

  return (
    <Button onClick={onClick} {...sharedProps}>
      {content}
    </Button>
  );
}

IconButton.propTypes = {
  action: PropTypes.oneOf(["view", "edit", "delete", "toggle", "renew"])
    .isRequired,
  tooltip: PropTypes.string.isRequired,
  onClick: PropTypes.func,
  to: PropTypes.string,
  size: PropTypes.oneOf(["sm", "md", "lg"]),
  variant: PropTypes.oneOf(["primary", "secondary", "ghost", "danger"]),
};
